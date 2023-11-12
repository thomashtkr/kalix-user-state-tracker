package be.htkr.jnj.kalix.demo.event.simulation.entities.user;

import kalix.javasdk.annotations.EventHandler;
import kalix.javasdk.annotations.Id;
import kalix.javasdk.annotations.TypeId;
import kalix.javasdk.eventsourcedentity.EventSourcedEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Id("userId")
@TypeId("user")
@RequestMapping("/user/{userId}")
public class UserEntity extends EventSourcedEntity<UserState, UserEvent> {

    private final Logger logger = LoggerFactory.getLogger(UserEntity.class);

    @Override
    public UserState emptyState(){
        return new UserState(null,  null, null, null, null, null);
    }

    @PostMapping("/register")
    public Effect<String> register(@RequestBody UserCommand.RegisterUser command) {
        if(currentState().name() != null) {
            return effects().error("user is already registered");
        }
        return effects()
                .emitEvent(new UserEvent.UserRegistered(commandContext().entityId(), command.name()))
                .thenReply(state -> "OK");
    }

    @EventHandler
    public UserState on(UserEvent.UserRegistered event) {
        return currentState().registerUser(eventContext().entityId(),event.name());
    }

    @PostMapping("/profile")
    public Effect<String> storeProfile(@RequestBody UserCommand.StoreUserProfile command) {
        return effects().emitEvent(new UserEvent.ProfileCompleted(commandContext().entityId(), command.favoriteColor(), command.country(), command.gender(), Instant.now(), command.birthdate()))
                .thenReply(state -> "OK");
    }

    @EventHandler
    public UserState on(UserEvent.ProfileCompleted event) {
        logger.info("profile completed for {}", eventContext().entityId());
        return currentState().completeProfile(event.favoriteColor(), event.birthDate());
    }

    @PostMapping("/gdpr")
    public Effect<String> confirmGdpr(@RequestBody UserCommand.ConfirmGdpr command) {
        return effects().emitEvent(new UserEvent.GdprConfirmed(commandContext().entityId(), command.termsAndConditionsId(), Instant.now()))
                .thenReply(state -> "OK");
    }

    @EventHandler
    public UserState on(UserEvent.GdprConfirmed event) {
        logger.info("gdprConfirmed for {}", eventContext().entityId());
        return currentState().confirmGdpr(event.termsAndCondition());
    }

    @PostMapping("/email")
    public Effect<String> verifyEmail(@RequestBody UserCommand.VerifyEmail command) {
        return effects().emitEvent(new UserEvent.EmailVerified(commandContext().entityId(), command.email(), Instant.now()))
                .thenReply(state -> "OK");
    }

    @EventHandler
    public UserState on(UserEvent.EmailVerified event) {
        logger.info("emailVerified for {}", eventContext().entityId());
        return currentState().verifyEmail(event.email());
    }

    @GetMapping("/name")
    public Effect<String> getName() {
        if(currentState().name() == null) {
            return effects().error("not yet registered");
        }
        return effects().reply(currentState().name());
    }
}
