package be.htkr.jnj.kalix.demo.action;

import be.htkr.jnj.kalix.demo.entity.user.UpdateUserStateCommand;
import be.htkr.jnj.kalix.demo.entity.user.UserState;
import be.htkr.jnj.kalix.demo.entity.user.UserStateEntity;
import be.htkr.jnj.kalix.demo.events.UserBusinessEvent;
import kalix.javasdk.action.Action;
import kalix.javasdk.annotations.Subscribe;
import kalix.javasdk.client.ComponentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static be.htkr.jnj.kalix.demo.DemoConfig.USER_BUSINESS_EVENTS_TOPIC;

public class TopicConsumeAction extends Action {

    private final Logger logger = LoggerFactory.getLogger(TopicConsumeAction.class);

    private final ComponentClient componentClient;

    public TopicConsumeAction(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    @Subscribe.Topic(value = USER_BUSINESS_EVENTS_TOPIC)
    public Effect<UserState.Status> onUserRegistered(UserBusinessEvent event) {
        logger.info("received event from topic {}", event);

        var forwardToEntity = componentClient.forEventSourcedEntity(event.userId().toString()).call(UserStateEntity::updateStatus)
                .params(new UpdateUserStateCommand(event));

        return effects().forward(forwardToEntity);
    }
    /*
    public Effect<UserState.Status> onUserRegistered(UserBusinessEvent.UserRegistered event) {
        logger.info("received event from topic {}", event);

        var forwardToEntity = componentClient.forEventSourcedEntity(event.user().id().toString()).call(UserStateEntity::updateStatus)
                .params(new UpdateUserStateCommand(event));

        return effects().forward(forwardToEntity);
    }

     */
}
