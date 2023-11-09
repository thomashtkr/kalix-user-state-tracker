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
import java.util.ArrayList;
import java.util.List;

@Id("userId")
@TypeId("user")
@RequestMapping("/user/{userId}")
public class UserStateEntity extends EventSourcedEntity<UserState, UserEntityStatusMovementEvent> {

    private final Logger logger = LoggerFactory.getLogger(UserStateEntity.class);

    @Override
    public UserState emptyState(){
        return new UserState(null, null, null, new UserDemographic(null, null, null));
    }

    @PostMapping("/update")
    public Effect<UserState.Status> updateStatus(@RequestBody UpdateUserStateCommand command) {
        logger.info("updateState with {}", command.event() );
        Instant now = Instant.now();
        List<UserEntityStatusMovementEvent> movements = new ArrayList<>();
        var demographic = command.getDemographic().orElse(null);
        //+1 for the new status
        movements.add(new UserEntityStatusMovementEvent(commandContext().entityId(), getStatusFromBusinessEvent(command.event()), 1, now, demographic));
        if(currentState().currentStatus() != null) {
            //-1 for the previousStatus status
            movements.add(new UserEntityStatusMovementEvent(commandContext().entityId(), currentState().currentStatus(), -1, now, demographic));
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
    public UserState on(UserEntityStatusMovementEvent event) {
        if(event.movement() > 0) {
            var newState =  currentState().updateStatus(event.status(), event.timestamp());
            if(event.demographic() != null){
                return newState.updateDemographic(event.demographic().favoriteColor(), event.demographic().country(), event.demographic().gender());
            } else {
                return newState;
            }
        } else {
            //the minus-events can be ignored. We only care about the new status
            return currentState();
        }
    }
}
