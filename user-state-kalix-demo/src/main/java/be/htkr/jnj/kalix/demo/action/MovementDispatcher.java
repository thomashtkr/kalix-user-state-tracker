package be.htkr.jnj.kalix.demo.action;

import be.htkr.jnj.kalix.demo.entity.dualevel.DualLevelGroupingEntity;
import be.htkr.jnj.kalix.demo.entity.singlelevel.RegisterStatusMovementCommand;
import be.htkr.jnj.kalix.demo.entity.singlelevel.SingleLevelGroupingEntity;
import be.htkr.jnj.kalix.demo.entity.user.AgeGroup;
import be.htkr.jnj.kalix.demo.entity.user.UserState;
import be.htkr.jnj.kalix.demo.entity.user.UserStateEntity;
import be.htkr.jnj.kalix.demo.entity.user.events.DemographicMovement;
import be.htkr.jnj.kalix.demo.entity.user.events.UserStatusMovement;
import be.htkr.jnj.kalix.demo.view.GroupingName;
import kalix.javasdk.action.Action;
import kalix.javasdk.annotations.Subscribe;
import kalix.javasdk.client.ComponentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static be.htkr.jnj.kalix.demo.view.GroupingName.PER_MONTH;
import static be.htkr.jnj.kalix.demo.view.GroupingName.PER_QUARTER;
import static be.htkr.jnj.kalix.demo.view.GroupingName.PER_YEAR;

@Subscribe.EventSourcedEntity(value = UserStateEntity.class)
public class MovementDispatcher extends Action {

    private final Logger logger = LoggerFactory.getLogger(MovementDispatcher.class);

    private final ComponentClient componentClient;

    public MovementDispatcher(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }


    public Effect<String> dispatchEvent(UserStatusMovement.UserStatusIncrement event) {
        return dispatchStatusMovement(event);
    }
    public Effect<String> dispatchEvent(UserStatusMovement.UserStatusDecrement event) {
        return dispatchStatusMovement(event);
    }

    public Effect<String> dispatchEvent(DemographicMovement.DemographicIncrement event) {
        return dispatchDemographicMovement(event);
    }
    public Effect<String> dispatchEvent(DemographicMovement.DemographicDecrement event) {
        return dispatchDemographicMovement(event);
    }

    private Effect<String> dispatchStatusMovement(UserStatusMovement event) {
        List<CompletionStage<String>> outboundRequests = new ArrayList<>(List.of(
                groupByPeriod(event, PER_YEAR),
                groupByPeriod(event, PER_MONTH),
                groupByPeriod(event, PER_QUARTER))
        );

        event.getAgeGroup().ifPresent(ageGroup -> {
            //if there is an agegroup, we need to dispatch the statusMovement to that ageGroup
            outboundRequests.add(groupByAgeGroup(event.userId(), ageGroup, event.status(), event.movement()));
        });

        CompletableFuture<String> all = CompletableFuture.allOf(outboundRequests.toArray(new CompletableFuture[0]))
                .thenApply(done -> "OK");

        return effects().asyncReply(all);
    }

    //eventHandler
    private Effect<String> dispatchDemographicMovement(DemographicMovement event) {
        logger.info("dispatchDemographicMovement {} movement {}", event.demographic(), event.movement());
        List<CompletionStage<String>> outboundRequests = new ArrayList<>();
        event.getAgeGroup().ifPresent(ageGroup -> {
            outboundRequests.add(groupByAgeGroup(event.userId(), ageGroup, event.status(), event.movement()));
        });

        //when the birthdate was registered, we schedule a future birthday event
        if(event.status().equals(UserState.Status.PROFILE_COMPLETE) && event.demographic() != null) {
            outboundRequests.add(scheduleBirthdayAction(event.userId(), event.demographic().birthDate()));
        }

        //group by gender and country
        if(event.demographic() != null){
            outboundRequests.add(groupByGenderAndCountry(event));
        }

        CompletableFuture<String> all = CompletableFuture.allOf(outboundRequests.toArray(new CompletableFuture[0]))
                .thenApply(done -> "OK");


        return effects()
                .asyncReply(all);
    }


    /**
     *
     * Dispatches an event to the StatusPerPeriodEntity for a certain groupName. The groupId is derived from the timestamp
     */
    private CompletionStage<String> groupByPeriod(UserStatusMovement event, GroupingName periodName) {
        String periodId = GroupingName.timeStampToPeriodId(event.timestamp(), periodName);
        logger.info("dispatching StatusMovement {} {} to group {}, {}", event.movement(), event.status(), periodName, periodId);
        return componentClient.forValueEntity(periodName.value, periodId)
                .call(SingleLevelGroupingEntity::registerMovement)
                .params(periodName.value, periodId, new RegisterStatusMovementCommand(event.status(), event.movement()))
                .execute();
    }

    private CompletionStage<String> groupByAgeGroup(String userId, AgeGroup ageGroup, UserState.Status status, int movement) {
        logger.info("dispatching {} {} to ageGroup grouping {} for user {}", movement, status, ageGroup.value, userId);
        return componentClient.forValueEntity(GroupingName.PER_AGEGROUP.value, ageGroup.value)
                .call(SingleLevelGroupingEntity::registerMovement)
                .params(GroupingName.PER_AGEGROUP.value, ageGroup.value, new RegisterStatusMovementCommand(status, movement))
                .execute();

    }

    private CompletionStage<String> groupByGenderAndCountry(DemographicMovement event) {
        String genderCountryId = GroupingName.dualGroupingKey(event.demographic().gender(), event.demographic().country());
        logger.info("grouping {} for gender/Country {}", event.status(), genderCountryId);
        return componentClient.forValueEntity(GroupingName.PER_GENDER.value, GroupingName.PER_COUNTRY.value, genderCountryId)
                .call(DualLevelGroupingEntity::registerMovement)
                .params(GroupingName.PER_GENDER.value, GroupingName.PER_COUNTRY.value, genderCountryId, new RegisterStatusMovementCommand(event.status(), event.movement()))
                .execute();

    }

    private CompletionStage<String> scheduleBirthdayAction(String userId, LocalDate birthDate) {
        return componentClient.forAction()
                .call(AgeGroupMovementAction::scheduleBirthdayAction)
                .params(userId, birthDate)
                .execute();
    }

}
