package be.htkr.jnj.kalix.demo.action;

import be.htkr.jnj.kalix.demo.entity.user.UserStateEntity;
import be.htkr.jnj.kalix.demo.entity.user.UserEntityStatusMovementEvent;
import be.htkr.jnj.kalix.demo.events.UserStatusMovement;
import kalix.javasdk.action.Action;
import kalix.javasdk.annotations.Publish;
import kalix.javasdk.annotations.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static be.htkr.jnj.kalix.demo.DemoConfig.STATUS_MOVEMENT_STREAM;

@Subscribe.EventSourcedEntity(UserStateEntity.class)
//@Publish.Stream(id = STATUS_MOVEMENT_STREAM)
//@Acl(allow = @Acl.Matcher(service = "*"))
public class StatusMovementPublisher extends Action {

    private final Logger logger = LoggerFactory.getLogger(StatusMovementPublisher.class);

    @Publish.Topic(STATUS_MOVEMENT_STREAM)
    public Effect<UserStatusMovement> publishMovement(UserEntityStatusMovementEvent event) {
        logger.info("publishing {}", event);
        String status = Optional.ofNullable(event.status()).map(Enum::name).orElse(null);
        return effects().reply(new UserStatusMovement(event.userId(), status, event.movement(), event.timestamp()));
    }
}
