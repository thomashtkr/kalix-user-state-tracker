package be.htkr.jnj.kalix.demo.action;

import be.htkr.jnj.kalix.demo.entity.statusperperiod.RegisterStatusMovementCommand;
import be.htkr.jnj.kalix.demo.entity.statusperperiod.StatusPerPeriodEntity;
import be.htkr.jnj.kalix.demo.entity.user.UserEntityEvent;
import be.htkr.jnj.kalix.demo.entity.user.UserStateEntity;
import be.htkr.jnj.kalix.demo.view.PeriodGroupingName;
import com.google.protobuf.any.Any;
import kalix.javasdk.DeferredCall;
import kalix.javasdk.SideEffect;
import kalix.javasdk.action.Action;
import kalix.javasdk.annotations.Subscribe;
import kalix.javasdk.client.ComponentClient;
import kalix.spring.WebClientProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static be.htkr.jnj.kalix.demo.view.PeriodGroupingName.PER_MONTH;
import static be.htkr.jnj.kalix.demo.view.PeriodGroupingName.PER_QUARTER;
import static be.htkr.jnj.kalix.demo.view.PeriodGroupingName.PER_YEAR;

@Subscribe.EventSourcedEntity(UserStateEntity.class)
public class StatusMovementToCounterDispatcher extends Action {

    private final Logger logger = LoggerFactory.getLogger(StatusMovementToCounterDispatcher.class);

    private final ComponentClient componentClient;

    public StatusMovementToCounterDispatcher(ComponentClient componentClient, WebClientProvider webClientProvider) {
        this.componentClient = componentClient;
    }

    public Effect<String> dispatchMovement(UserEntityEvent.UserStatusMovementEvent event) {
        return effects().reply("")
                .addSideEffect(SideEffect.of(groupByPeriod(event, PER_YEAR), true),
                                SideEffect.of(groupByPeriod(event, PER_MONTH), true),
                                SideEffect.of(groupByPeriod(event, PER_QUARTER), true));
    }


    /**
     *
     * Dispatches an event to the StatusPerPeriodEntity for a certain periodName. The periodId is derived from the timestamp
     */
    private DeferredCall<Any, String> groupByPeriod(UserEntityEvent event, PeriodGroupingName periodName) {
        String periodId = PeriodGroupingName.timeStampToPeriodId(event.timestamp(), periodName);
        logger.info("dispatching StatusMovement to group {}, {}", periodName, periodId);
        return componentClient.forValueEntity(periodName.value, periodId)
                .call(StatusPerPeriodEntity::registerMovement)
                .params(periodName.value, periodId, new RegisterStatusMovementCommand(event.status(), event.movement()));
    }


}
