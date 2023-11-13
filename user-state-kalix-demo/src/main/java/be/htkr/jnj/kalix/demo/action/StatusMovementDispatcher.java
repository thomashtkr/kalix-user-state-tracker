package be.htkr.jnj.kalix.demo.action;

import be.htkr.jnj.kalix.demo.entity.singlelevel.RegisterStatusMovementCommand;
import be.htkr.jnj.kalix.demo.entity.singlelevel.SingleLevelGroupingEntity;
import be.htkr.jnj.kalix.demo.entity.user.UserEntityEvent;
import be.htkr.jnj.kalix.demo.entity.user.UserState;
import be.htkr.jnj.kalix.demo.entity.user.UserStateEntity;
import be.htkr.jnj.kalix.demo.view.GroupingName;
import com.google.protobuf.any.Any;
import kalix.javasdk.DeferredCall;
import kalix.javasdk.SideEffect;
import kalix.javasdk.action.Action;
import kalix.javasdk.annotations.Subscribe;
import kalix.javasdk.client.ComponentClient;
import kalix.spring.WebClientProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static be.htkr.jnj.kalix.demo.view.GroupingName.PER_MONTH;
import static be.htkr.jnj.kalix.demo.view.GroupingName.PER_QUARTER;
import static be.htkr.jnj.kalix.demo.view.GroupingName.PER_YEAR;

@Subscribe.EventSourcedEntity(value = UserStateEntity.class, ignoreUnknown = true)
public class StatusMovementDispatcher extends Action {

    private final Logger logger = LoggerFactory.getLogger(StatusMovementDispatcher.class);

    private final ComponentClient componentClient;

    public StatusMovementDispatcher(ComponentClient componentClient, WebClientProvider webClientProvider) {
        this.componentClient = componentClient;
    }

    public Effect<String> dispatchMovement(UserEntityEvent.UserStatusMovementEvent event) {
        List<SideEffect> allEffects = new ArrayList<>(List.of(
                SideEffect.of(groupByPeriod(event, PER_YEAR), true),
                SideEffect.of(groupByPeriod(event, PER_MONTH), true),
                SideEffect.of(groupByPeriod(event, PER_QUARTER), true)));

        groupByAgeGroup(event).ifPresent(perAgeGroupCall -> allEffects.add(SideEffect.of(perAgeGroupCall)));

        if(event.status().equals(UserState.Status.PROFILE_COMPLETE)) {
            allEffects.add(SideEffect.of(scheduleBirthdayAction(event.userId(), event.demographic().birthDate())));
        }

        return effects().reply("OK")
                .addSideEffect(allEffects.toArray(new SideEffect[0]));
    }


    /**
     *
     * Dispatches an event to the StatusPerPeriodEntity for a certain groupName. The groupId is derived from the timestamp
     */
    private DeferredCall<Any, String> groupByPeriod(UserEntityEvent.UserStatusMovementEvent event, GroupingName periodName) {
        String periodId = GroupingName.timeStampToPeriodId(event.timestamp(), periodName);
        logger.info("dispatching StatusMovement to group {}, {}", periodName, periodId);
        return componentClient.forValueEntity(periodName.value, periodId)
                .call(SingleLevelGroupingEntity::registerMovement)
                .params(periodName.value, periodId, new RegisterStatusMovementCommand(event.status(), event.movement()));
    }

    private Optional<DeferredCall<Any, String>> groupByAgeGroup(UserEntityEvent.UserStatusMovementEvent event) {
        return event.getAgeGroup().map(ageGroup -> {
            return componentClient.forValueEntity(GroupingName.PER_AGEGROUP.value, ageGroup.value)
                    .call(SingleLevelGroupingEntity::registerMovement)
                    .params(GroupingName.PER_AGEGROUP.value, ageGroup.value, new RegisterStatusMovementCommand(event.status(), event.movement()));
        });
    }

    private DeferredCall<Any, String> scheduleBirthdayAction(String userId, LocalDate birthDate) {
        return componentClient.forAction()
                .call(AgeGroupMovementAction::scheduleBirthdayAction)
                .params(userId, birthDate);
    }


}
