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
        diffs.forEach(d -> logger.info("difference  " + d));
        if(diffs.isEmpty()){
            return effects().reply(currentState().currentStatus());
        } else {
            return effects().emitEvents(diffs)
                    .thenReply(UserState::currentStatus);
        }

        /*
        if(currentDemographic != null && !Objects.equals(currentState().currentDemographic().ageGroup(), currentDemographic.ageGroup())) {

        }
        UserState.Status newStatus = getStatusFromBusinessEvent(command.event());
        if(! Objects.equals(newStatus, currentState().currentStatus() )) {
            List<UserEntityEvent> movements = new ArrayList<>();
            //+1 for the new status
            movements.add(new UserStatusMovement.UserStatusIncrement(commandContext().entityId(), newStatus, now));
            if(currentState().currentStatus() != null) {
                //-1 for the previousStatus status
                movements.add(new UserStatusMovement.UserStatusDecrement(commandContext().entityId(), currentState().currentStatus(), now));
            }
            return effects().emitEvents(movements)
                    .thenReply(UserState::currentStatus);
        } else {
            //deduplication. Commands (based of a topic) can be delivered multiple times
            return effects().reply(currentState().currentStatus());
        }

         */

    }

    private static List<UserEntityEvent> differencesWithCurrentState(UserState currentState, UpdateUserStateCommand command, String entityId) {
        final List<UserEntityEvent> diffs = new ArrayList<>();
        Instant now = Instant.now();

        UserState.Status newStatus = getStatusFromBusinessEvent(command.event());
        if( !Objects.equals(newStatus, currentState.currentStatus() )) {
            List<UserEntityEvent> statusMovements = new ArrayList<>();
            //+1 for the new status
            statusMovements.add(new UserStatusMovement.UserStatusIncrement(entityId, newStatus, now));
            if(currentState.currentStatus() != null) {
                //-1 for the previousStatus status
                statusMovements.add(new UserStatusMovement.UserStatusDecrement(entityId, currentState.currentStatus(), now));
            }
            diffs.addAll(statusMovements);
        }

        List<UserEntityEvent> demographicMovements = command.getDemographic().map(newDemographic -> {
            List<UserEntityEvent> movements = new ArrayList<>();
            UserDemographic currentDemographic = currentState.currentDemographic();
            //every updated demographic should result in a separate event.
            //only ageGroup is implemented here
            if(!Objects.equals(newDemographic, currentDemographic)) {
                var currentAgeGroup = currentDemographic.ageGroup();
                if(!Objects.equals(currentAgeGroup, newDemographic.ageGroup())){
                    if(currentAgeGroup != null){
                        movements.add(new DemographicMovement.DemographicDecrement(entityId, currentState.currentStatus(), Instant.now(), currentDemographic));
                    }
                    movements.add(new DemographicMovement.DemographicIncrement(entityId, newStatus, Instant.now(), newDemographic));
                }
            }
            return movements;
        }).orElse(List.of());
        diffs.addAll(demographicMovements);
        return diffs;
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
        logger.info("updating state with " + increment);
        var current =  currentState().updateStatus(increment.status(), increment.timestamp());
        logger.info("current" + current);
        return current;
        /*
        if(increment.currentDemographic() != null){
            return newState.updateDemographic(increment.currentDemographic().favoriteColor(), increment.currentDemographic().country(), increment.currentDemographic().gender(), increment.currentDemographic().birthDate(), increment.currentDemographic().ageGroup())     ;
        } else {
            return newState;
        }

         */
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
        logger.info("updating state with " + increment);
        UserDemographic demographic = increment.demographic();
        var current =   currentState().updateDemographic(demographic);
        logger.info("current" + current);
        return current;
    }

    @EventHandler
    public UserState on(DemographicMovement.DemographicDecrement decrement) {
        //deliberately left empty
        return currentState();
    }

}
