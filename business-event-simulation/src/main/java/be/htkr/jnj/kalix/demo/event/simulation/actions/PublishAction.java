package be.htkr.jnj.kalix.demo.event.simulation.actions;

import be.htkr.jnj.kalix.demo.event.simulation.entities.user.UserEntity;
import be.htkr.jnj.kalix.demo.event.simulation.entities.user.UserEvent;
import be.htkr.jnj.kalix.demo.events.UserBusinessEvent;
import be.htkr.jnj.kalix.demo.events.model.User;
import be.htkr.jnj.kalix.demo.events.model.UserDetails;
import kalix.javasdk.action.Action;
import kalix.javasdk.annotations.Publish;
import kalix.javasdk.annotations.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;


@Subscribe.EventSourcedEntity(value = UserEntity.class)
public class PublishAction extends Action {

    private final Logger logger = LoggerFactory.getLogger(PublishAction.class);


    @Publish.Topic("user-events")
    public Effect<UserBusinessEvent> publishUserRegisteredEvent(UserEvent.UserRegistered internalEvent) {
        logger.info("publishing {}", internalEvent);
        return effects()
                .reply(new UserBusinessEvent.UserRegistered(internalEvent.userId(), new User(internalEvent.userId(), internalEvent.name(), null ), Instant.now()));
    }

    @Publish.Topic("user-events")
    public Effect<UserBusinessEvent> publishUserProfileCompletedEvent(UserEvent.ProfileCompleted internalEvent) {
        logger.info("publishing {}", internalEvent);
        return effects()
                .reply(new UserBusinessEvent.UserProfileCompleted(internalEvent.userId(),
                        new UserDetails(internalEvent.favoriteColor(), internalEvent.country(), internalEvent.gender(), internalEvent.birthDate()),
                        internalEvent.timestamp()));
    }

    @Publish.Topic("user-events")
    public Effect<UserBusinessEvent> publishUserGdprConfirmedEvent(UserEvent.GdprConfirmed internalEvent) {
        logger.info("publishing {}", internalEvent);
        return effects()
                .reply(new UserBusinessEvent.UserGdprConsentConfirmed(internalEvent.userid(), internalEvent.timestamp(), internalEvent.termsAndCondition()));
    }

    @Publish.Topic("user-events")
    public Effect<UserBusinessEvent> publishEmailVerifiedEvent(UserEvent.EmailVerified internalEvent) {
        logger.info("publishing {}", internalEvent);
        return effects()
                .reply(new UserBusinessEvent.UserVerified(internalEvent.userId(),internalEvent.email(), internalEvent.timestamp()));
    }
}
