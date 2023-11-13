package be.htkr.jnj.kalix.demo;

import be.htkr.jnj.kalix.demo.entity.user.UserState;
import be.htkr.jnj.kalix.demo.events.UserBusinessEvent;
import be.htkr.jnj.kalix.demo.events.UserStatusMovement;
import be.htkr.jnj.kalix.demo.events.model.User;
import be.htkr.jnj.kalix.demo.events.model.UserDetails;
import be.htkr.jnj.kalix.demo.view.PeriodGroupingName;
import be.htkr.jnj.kalix.demo.view.SingleLevelGroupedViewData;
import kalix.javasdk.testkit.EventingTestKit;
import kalix.javasdk.testkit.KalixTestKit;
import kalix.spring.testkit.KalixIntegrationTestKitSupport;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static be.htkr.jnj.kalix.demo.DemoConfig.STATUS_MOVEMENT_STREAM;
import static be.htkr.jnj.kalix.demo.view.PeriodGroupingName.PER_MONTH;
import static be.htkr.jnj.kalix.demo.view.PeriodGroupingName.PER_QUARTER;
import static be.htkr.jnj.kalix.demo.view.PeriodGroupingName.PER_YEAR;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = Main.class)
@ExtendWith(MockitoExtension.class)
@Import(TestTopicConfig.class)
public class ConsumeIntegrationTest extends KalixIntegrationTestKitSupport {

    @Autowired
    private KalixTestKit kalixTestKit;


    private EventingTestKit.IncomingMessages eventsTopic;

    private EventingTestKit.OutgoingMessages movementsStream;

    @Autowired
    private WebClient webClient;


    @BeforeAll
    public void beforeAll() throws IOException {
        eventsTopic = kalixTestKit.getTopicIncomingMessages(DemoConfig.USER_BUSINESS_EVENTS_TOPIC);
        movementsStream = kalixTestKit.getTopicOutgoingMessages(STATUS_MOVEMENT_STREAM);
    }


    @Test
    public void verifyCounterEventSourcedPublishToTopicAndProjectedInView() throws Exception {
        var topicId = "user-events";
        var userId = UUID.randomUUID().toString();
        var birthDate = LocalDate.now();
        var registeredUser = new UserBusinessEvent.UserRegistered(userId, new User(userId, "name", "email"), Instant.now());
        eventsTopic.publish(registeredUser, topicId);
        eventsTopic.publish(new UserBusinessEvent.UserProfileCompleted(userId, new UserDetails("blue", "BE", "M", birthDate), Instant.now()), topicId);
        UserStatusMovement reg = movementsStream.expectOneTyped(UserStatusMovement.class, Duration.of(5, ChronoUnit.SECONDS)).getPayload();
        assertThat(reg.movement()).isEqualTo(1);
        assertThat(reg.status()).isEqualTo(UserState.Status.REGISTERED.name());

        UserStatusMovement completed = movementsStream.expectOneTyped(UserStatusMovement.class).getPayload();
        assertThat(completed.movement()).isEqualTo(1);
        assertThat(completed.status()).isEqualTo(UserState.Status.PROFILE_COMPLETE.name());

        UserStatusMovement unRegistered = movementsStream.expectOneTyped(UserStatusMovement.class).getPayload();
        assertThat(unRegistered.movement()).isEqualTo(-1);
        assertThat(unRegistered.status()).isEqualTo(UserState.Status.REGISTERED.name());

        assertThat(movementsStream.clear()).isEmpty();

        Thread.sleep(5000L);
        Instant now = Instant.now();
        String currentYear = PeriodGroupingName.timeStampToPeriodId(now, PER_YEAR);

        List<SingleLevelGroupedViewData> perYearResponse = getViewDataFor(PER_YEAR);
        verifyPerPeriod(perYearResponse, PER_YEAR, currentYear);

        String currentMonth = PeriodGroupingName.timeStampToPeriodId(now, PER_MONTH);
        List<SingleLevelGroupedViewData> perMonthResponse = getViewDataFor(PER_MONTH);
        verifyPerPeriod(perMonthResponse, PER_MONTH, currentMonth);

        String currentQuarter = PeriodGroupingName.timeStampToPeriodId(now, PER_QUARTER);
        List<SingleLevelGroupedViewData> perQuarterResponse = getViewDataFor(PER_QUARTER);
        verifyPerPeriod(perQuarterResponse, PER_QUARTER, currentQuarter);



/*
        StatusPerPeriodViewData resultPerYear = componentClient.forView()
                .call(StatusPerPeriodView::getStatusPerPeriod)
                .params(PER_YEAR.value, currentYear)
                .execute()
                .toCompletableFuture().get();

        System.out.println("response " + resultPerYear);
        String currentMonth = PeriodGroupingName.timeStampToPeriodId(now, PER_YEAR);
        StatusPerPeriodViewData resultPerMonth = componentClient.forView().call(StatusPerPeriodView::getStatusPerPeriod)
                .params(PeriodGroupingName.PER_MONTH.name(), currentMonth)
                .execute()
                .toCompletableFuture().get();

 */
    }

    private void verifyPerPeriod(List<SingleLevelGroupedViewData> perYearResponse, PeriodGroupingName periodName, String periodId) {
        var perPeriod = perYearResponse.get(0);
        assertThat(perPeriod.groupName()).isEqualTo(periodName.value);
        assertThat(perPeriod.groupId()).isEqualTo(periodId);
        assertThat(perPeriod.counters()).hasSize(2);
        assertThat(perPeriod.counters().stream().filter(c -> c.status().equals("REGISTERED")).count()).isEqualTo(1);
        assertThat(perPeriod.counters().stream().filter(c -> c.status().equals("PROFILE_COMPLETE")).count()).isEqualTo(1);
    }

    private List<SingleLevelGroupedViewData> getViewDataFor(PeriodGroupingName periodName) {
        return webClient.get().uri("/view/counters/{groupName}", Map.of("groupName", periodName.value))
                .retrieve().bodyToFlux(SingleLevelGroupedViewData.class).collectList().block();
    }
}
