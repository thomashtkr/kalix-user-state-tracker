package be.htkr.jnj.kalix.demo.entity.user;

import be.htkr.jnj.kalix.demo.entity.user.events.DemographicMovement;
import be.htkr.jnj.kalix.demo.entity.user.events.UserEntityEvent;
import be.htkr.jnj.kalix.demo.entity.user.events.UserStatusMovement;
import be.htkr.jnj.kalix.demo.events.UserBusinessEvent;
import kalix.javasdk.annotations.EventHandler;
import kalix.javasdk.annotations.Id;
import kalix.javasdk.annotations.TypeId;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Id("userId")
@TypeId("user")
@RequestMapping("/user/{userId}")
public class UserStateEntity extends EventSourcedEntity<UserState, UserEntityEvent> {

    private final Logger logger = LoggerFactory.getLogger(UserStateEntity.class);

    @Override
    public UserState emptyState(){
        return new UserState();
    }

    @PostMapping("/update")
    public Effect<UserState.Status> updateUserState(@RequestBody UpdateUserStateCommand command) {
        logger.info("updateState with {}", command.event() );
        var diffs = differencesWithCurrentState(currentState(), command, commandContext().entityId());
        logger.info("found {} differences ", diffs.size());
        diffs.forEach(d -> logger.info("  " + d));
        if(diffs.isEmpty()){
            return effects().reply(currentState().currentStatus());
        } else {
            return effects().emitEvents(diffs)
                    .thenReply(UserState::currentStatus);
        }

    }

    private static List<UserEntityEvent> differencesWithCurrentState(UserState currentState, UpdateUserStateCommand command, String entityId) {
        final List<UserEntityEvent> diffs = new ArrayList<>();
        diffs.addAll(getUserStateDifferences(entityId, currentState, command));
        diffs.addAll(getDemographicDifferences(entityId, currentState, command));
        return diffs;
    }
    private static List<UserEntityEvent> getUserStateDifferences(String entityId, UserState currentState, UpdateUserStateCommand command) {
        UserState.Status newStatus = getStatusFromBusinessEvent(command.event());
        UserDemographic currentDemographic = currentState.currentDemographic();
        if( !Objects.equals(newStatus, currentState.currentStatus() )) {
            List<UserEntityEvent> statusMovements = new ArrayList<>();
            //+1 for the new status with the current demographic
            statusMovements.add(new UserStatusMovement.UserStatusIncrement(entityId, newStatus, currentDemographic, Instant.now()));
            if(currentState.currentStatus() != null) {
                //-1 for the previousStatus status with the current demographic
                statusMovements.add(new UserStatusMovement.UserStatusDecrement(entityId, currentState.currentStatus(), currentDemographic, Instant.now()));
            }
            return statusMovements;
        }
        return List.of();
    }

    private static List<UserEntityEvent> getDemographicDifferences(String entityId, UserState currentState, UpdateUserStateCommand command) {
        UserState.Status newStatus = getStatusFromBusinessEvent(command.event());
        UserDemographic currentDemographic = currentState.currentDemographic();

        return command.getDemographic().map(newDemographic -> {
            List<UserEntityEvent> movements = new ArrayList<>();

            //every updated demographic should result in a separate event.
            //only ageGroup is implemented here
            if(!Objects.equals(newDemographic, currentDemographic)) {
                var currentAgeGroup = currentDemographic.ageGroup();
                if(!Objects.equals(currentAgeGroup, newDemographic.ageGroup())){
                    //+1 for the newDemographic (with the new status: the status is already updated when this event is processed)
                    movements.add(new DemographicMovement.DemographicIncrement(entityId, newStatus, Instant.now(), newDemographic));
                    if(currentAgeGroup != null){
                        //-1 for the previousDemographic (with the new status: the status is already updated when this event is processed)
                        movements.add(new DemographicMovement.DemographicDecrement(entityId, newStatus, Instant.now(), currentDemographic));
                    }
                }
            }
            return movements;
        }).orElse(List.of());
    }




    private static UserState.Status getStatusFromBusinessEvent(UserBusinessEvent event) {
        return switch (event){
            case UserBusinessEvent.UserRegistered registered -> UserState.Status.REGISTERED;
            case UserBusinessEvent.UserGdprConsentConfirmed confirmed -> UserState.Status.GDPR_CONFIRMED;
            case UserBusinessEvent.UserProfileCompleted completed -> UserState.Status.PROFILE_COMPLETE;
            case UserBusinessEvent.UserVerified verified -> UserState.Status.EMAIL_VERIFIED;
        };
    }


    @EventHandler
    public UserState on(UserStatusMovement.UserStatusIncrement increment) {
        return currentState().updateStatus(increment.status(), increment.timestamp());
    }

    @EventHandler
    public UserState on(UserStatusMovement.UserStatusDecrement decrement) {
        //deliberately left empty
        return currentState();
    }

    @PostMapping("/update-age-group")
    public Effect<UserState.Status> updateAgeGroup(@RequestBody UpdateAgeGroupCommand command) {
        logger.info("updating ageGroup of {}", commandContext().entityId());
        var currentAgeGroup = currentState().currentDemographic().ageGroup();
        long age = ChronoUnit.YEARS.between(currentState().currentDemographic().birthDate(), LocalDate.now());
        var newAgeGroup = AgeGroup.getAgeGroup(age);
        if(currentAgeGroup != newAgeGroup) {
            logger.info("user {} moved to different ageGroup", commandContext().entityId());
            List<UserEntityEvent> movements = new ArrayList<>();
            movements.add(new DemographicMovement.DemographicIncrement(commandContext().entityId(), currentState().currentStatus(), Instant.now(), currentState().currentDemographic().updateAgeGroup(newAgeGroup)));
            movements.add(new DemographicMovement.DemographicDecrement(commandContext().entityId(), currentState().currentStatus(), Instant.now(), currentState().currentDemographic()));
            return effects().emitEvents(movements)
                    .thenReply(UserState::currentStatus);
        } else {
            return effects().reply(currentState().currentStatus());
        }
    }

    @EventHandler
    public UserState on(DemographicMovement.DemographicIncrement increment) {
        UserDemographic demographic = increment.demographic();
        return currentState().updateDemographic(demographic);
    }

    @EventHandler
    public UserState on(DemographicMovement.DemographicDecrement decrement) {
        //deliberately left empty
        return currentState();
    }

}
