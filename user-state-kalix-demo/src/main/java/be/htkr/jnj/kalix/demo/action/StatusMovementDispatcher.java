package be.htkr.jnj.kalix.demo.action;

import be.htkr.jnj.kalix.demo.entity.dualevel.DualLevelGroupingEntity;
import be.htkr.jnj.kalix.demo.entity.singlelevel.RegisterStatusMovementCommand;
import be.htkr.jnj.kalix.demo.entity.singlelevel.SingleLevelGroupingEntity;
import be.htkr.jnj.kalix.demo.entity.user.AgeGroup;
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

@Subscribe.EventSourcedEntity(value = UserStateEntity.class)
public class StatusMovementDispatcher extends Action {

    private final Logger logger = LoggerFactory.getLogger(StatusMovementDispatcher.class);

    private final ComponentClient componentClient;

    public StatusMovementDispatcher(ComponentClient componentClient, WebClientProvider webClientProvider) {
        this.componentClient = componentClient;
    }

    //eventHandler
    public Effect<String> dispatchStatusMovement(UserEntityEvent.UserStatusMovementEvent event) {
        List<SideEffect> allEffects = new ArrayList<>(List.of(
                SideEffect.of(groupByPeriod(event, PER_YEAR), true),
                SideEffect.of(groupByPeriod(event, PER_MONTH), true),
                SideEffect.of(groupByPeriod(event, PER_QUARTER), true)));


        event.getAgeGroup().ifPresent(ageGroup -> {
            allEffects.add(SideEffect.of(groupByAgeGroup(ageGroup, event.status(), event.movement()), true));
        });

        //when the birthdate was registered, we schedule a future birthday event
        if(event.status().equals(UserState.Status.PROFILE_COMPLETE) && event.demographic() != null) {
            allEffects.add(SideEffect.of(scheduleBirthdayAction(event.userId(), event.demographic().birthDate())));
        }

        //group by gender and country
        if(event.demographic() != null){
            allEffects.add(SideEffect.of(groupByGenderAndCountry(event)));
        }

        return effects().reply("OK")
                .addSideEffect(allEffects.toArray(new SideEffect[0]));
    }
    //eventHandler
    public Effect<String> dispatchAgeGroupMovement(UserEntityEvent.UserAgeGroupMovementEvent event) {
        logger.info("dispatchAgeGroupMovement {} movement {}", event.ageGroup(), event.movement());
        return effects()
                .reply("OK")
                .addSideEffect(SideEffect.of(groupByAgeGroup(event.ageGroup(), event.status(), event.movement()), true));
    }


    /**
     *
     * Dispatches an event to the StatusPerPeriodEntity for a certain groupName. The groupId is derived from the timestamp
     */
    private DeferredCall<Any, String> groupByPeriod(UserEntityEvent.UserStatusMovementEvent event, GroupingName periodName) {
        String periodId = GroupingName.timeStampToPeriodId(event.timestamp(), periodName);
        logger.info("dispatching StatusMovement {} {} to group {}, {}", event.movement(), event.status(), periodName, periodId);
        return componentClient.forValueEntity(periodName.value, periodId)
                .call(SingleLevelGroupingEntity::registerMovement)
                .params(periodName.value, periodId, new RegisterStatusMovementCommand(event.status(), event.movement()));
    }

    private DeferredCall<Any, String> groupByAgeGroup(AgeGroup ageGroup, UserState.Status status, int movement) {
        logger.info("dispatching {} {} to ageGroup grouping {}", movement, status, ageGroup.value);
        return componentClient.forValueEntity(GroupingName.PER_AGEGROUP.value, ageGroup.value)
                .call(SingleLevelGroupingEntity::registerMovement)
                .params(GroupingName.PER_AGEGROUP.value, ageGroup.value, new RegisterStatusMovementCommand(status, movement));

    }

    private DeferredCall<Any, String> groupByGenderAndCountry(UserEntityEvent.UserStatusMovementEvent event) {
        String genderCountryId = GroupingName.dualGroupingKey(event.demographic().gender(), event.demographic().country());
        logger.info("grouping {} for gender/Country {}", event.status(), genderCountryId);
        return componentClient.forValueEntity(GroupingName.PER_GENDER.value, GroupingName.PER_COUNTRY.value, genderCountryId)
                .call(DualLevelGroupingEntity::registerMovement)
                .params(GroupingName.PER_GENDER.value, GroupingName.PER_COUNTRY.value, genderCountryId, new RegisterStatusMovementCommand(event.status(), event.movement()));

    }


    private DeferredCall<Any, String> scheduleBirthdayAction(String userId, LocalDate birthDate) {
        return componentClient.forAction()
                .call(AgeGroupMovementAction::scheduleBirthdayAction)
                .params(userId, birthDate);
    }




}
