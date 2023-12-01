package be.htkr.jnj.kalix.demo.action;

import akka.Done;
import be.htkr.jnj.kalix.demo.entity.user.UpdateAgeGroupCommand;
import be.htkr.jnj.kalix.demo.entity.user.UserState;
import be.htkr.jnj.kalix.demo.entity.user.UserStateEntity;
import com.google.protobuf.any.Any;
import kalix.javasdk.DeferredCall;
import kalix.javasdk.action.Action;
import kalix.javasdk.client.ComponentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletionStage;

public class AgeGroupMovementAction extends Action {

    private final Logger logger = LoggerFactory.getLogger(AgeGroupMovementAction.class);

    private final ComponentClient componentClient;

    public AgeGroupMovementAction(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    @PostMapping("/internal/schedule/update-age-group/{userId}")
    public Effect<String> scheduleBirthdayAction(@PathVariable("userId") String userId, @RequestBody LocalDate birthDate) {
        var today = LocalDate.now();
        logger.info("scheduling updateAgeGroup for user {} with {}", userId, birthDate);
        var birthDay = getNextBirthDay(today, birthDate);
        var daysToBirthDay = ChronoUnit.DAYS.between(today, birthDay);
        logger.info("scheduling updateAgeGroup in {} days for user {}", daysToBirthDay, userId);
        String birthdayTimerName = userId + "_birthday";

        CompletionStage<Done> cancellationPreviousTimer = timers().cancel(birthdayTimerName);

        DeferredCall<Any,String> triggerUpdateAgeGroup = componentClient.forAction()
                .call(AgeGroupMovementAction::triggerUpdateAgeGroup)
                .params(userId, birthDate);

        CompletionStage<String> scheduleNextTimer = cancellationPreviousTimer
                .thenCompose(done -> {
                    return timers().startSingleTimer(birthdayTimerName, Duration.ofDays(daysToBirthDay/2), triggerUpdateAgeGroup);
                })
                .thenApply(done -> "Ok");


        return effects().asyncReply(scheduleNextTimer);
    }

    private LocalDate getNextBirthDay(LocalDate today, LocalDate born) {
        var next = born.withYear(today.getYear());
        if(!next.isAfter(today)){
            return next.plusYears(1);
        } else {
            return next;
        }
    }

    @PostMapping("/internal/trigger/update-age-group/{userId}")
    public Effect<String> triggerUpdateAgeGroup(@PathVariable("userId") String userId, @RequestBody LocalDate birthDate) {
        logger.info("triggering updateAgeGroup for user {}", userId);
        CompletionStage<UserState.Status> updateAgeGroup = componentClient.forEventSourcedEntity(userId)
                .call(UserStateEntity::updateAgeGroup)
                .params(new UpdateAgeGroupCommand())
                .execute();


        CompletionStage<String> scheduleNext = componentClient.forAction()
                .call(AgeGroupMovementAction::scheduleBirthdayAction)
                        .params(userId, birthDate).execute();

        return effects()
                .asyncReply(updateAgeGroup.thenCompose(done -> scheduleNext)
                .thenApply(done -> "Ok"));

    }


}
