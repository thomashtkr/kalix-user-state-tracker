package be.htkr.jnj.kalix.demo.entity.user;

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

@Id("userId")
@TypeId("user")
@RequestMapping("/user/{userId}")
public class UserStateEntity extends EventSourcedEntity<UserState, UserEntityEvent> {

    private final Logger logger = LoggerFactory.getLogger(UserStateEntity.class);

    @Override
    public UserState emptyState(){
        return new UserState(null, null, null, new UserDemographic(null, null, null, null, null));
    }

    @PostMapping("/update")
    public Effect<UserState.Status> updateStatus(@RequestBody UpdateUserStateCommand command) {
        logger.info("updateState with {}", command.event() );
        Instant now = Instant.now();
        List<UserEntityEvent.UserStatusMovementEvent> movements = new ArrayList<>();
        UserDemographic demographic = command.getDemographic().orElse(null);
        //+1 for the new status
        UserState.Status newStatus = getStatusFromBusinessEvent(command.event());
        movements.add(new UserEntityEvent.UserStatusMovementEvent(commandContext().entityId(), newStatus, 1, now, demographic));
        if(currentState().currentStatus() != null) {
            //-1 for the previousStatus status
            movements.add(new UserEntityEvent.UserStatusMovementEvent(commandContext().entityId(), currentState().currentStatus(), -1, now, demographic));
        }
        return effects().emitEvents(movements)
                .thenReply(UserState::currentStatus);
    }

    private UserState.Status getStatusFromBusinessEvent(UserBusinessEvent event) {
        return switch (event){
            case UserBusinessEvent.UserRegistered registered -> UserState.Status.REGISTERED;
            case UserBusinessEvent.UserGdprConsentConfirmed confirmed -> UserState.Status.GDPR_CONFIRMED;
            case UserBusinessEvent.UserProfileCompleted completed -> UserState.Status.PROFILE_COMPLETE;
            case UserBusinessEvent.UserVerified verified -> UserState.Status.EMAIL_VERIFIED;
        };
    }


    @EventHandler
    public UserState on(UserEntityEvent.UserStatusMovementEvent event) {
        if(event.movement() > 0) {
            var newState =  currentState().updateStatus(event.status(), event.timestamp());
            if(event.demographic() != null){
                return newState.updateDemographic(event.demographic().favoriteColor(), event.demographic().country(), event.demographic().gender(), event.demographic().birthDate(), event.demographic().ageGroup())     ;
            } else {
                return newState;
            }
        } else {
            //the minus-events can be ignored. We only care about the new status
            return currentState();
        }
    }

    @PostMapping("/update-age-group")
    public Effect<UserState.Status> updateAgeGroup(@RequestBody UpdateAgeGroupCommand command) {
        logger.info("updating ageGroup of {}", commandContext().entityId());
        var currentAgeGroup = currentState().demographic().ageGroup();
        long age = ChronoUnit.YEARS.between(currentState().demographic().birthDate(), LocalDate.now());
        var newAgeGroup = AgeGroup.getAgeGroup(age);
        if(currentAgeGroup != newAgeGroup) {
            logger.info("user {} moved to different ageGroup", commandContext().entityId());
            List<UserEntityEvent.UserAgeGroupMovementEvent> movements = new ArrayList<>();
            movements.add(new UserEntityEvent.UserAgeGroupMovementEvent(commandContext().entityId(), 1, Instant.now(), currentState().demographic(), newAgeGroup));
            movements.add(new UserEntityEvent.UserAgeGroupMovementEvent(commandContext().entityId(), -1, Instant.now(), currentState().demographic(), currentAgeGroup));
            return effects().emitEvents(movements)
                    .thenReply(UserState::currentStatus);
        } else {
            return effects().reply(currentState().currentStatus());
        }
    }

    @EventHandler
    public UserState on(UserEntityEvent.UserAgeGroupMovementEvent event) {
        if(event.movement() > 0) {
            return  currentState().updateAgeGroup(event.ageGroup());
        }else {
            //the minus-events can be ignored. We only care about the new status
            return currentState();
        }
    }

}
