package be.htkr.jnj.kalix.demo;

import be.htkr.jnj.kalix.demo.entity.user.AgeGroup;
import be.htkr.jnj.kalix.demo.entity.user.UserState;
import be.htkr.jnj.kalix.demo.events.UserBusinessEvent;
import be.htkr.jnj.kalix.demo.events.UserStatusMovement;
import be.htkr.jnj.kalix.demo.events.model.User;
import be.htkr.jnj.kalix.demo.events.model.UserDetails;
import be.htkr.jnj.kalix.demo.view.GroupingName;
import be.htkr.jnj.kalix.demo.view.dual.DualLevelGroupViewResponse;
import be.htkr.jnj.kalix.demo.view.dual.DualLevelGroupedViewData;
import be.htkr.jnj.kalix.demo.view.singlelevel.SingleLevelGroupViewResponse;
import be.htkr.jnj.kalix.demo.view.singlelevel.SingleLevelGroupedViewData;
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
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static be.htkr.jnj.kalix.demo.DemoConfig.STATUS_MOVEMENT_STREAM;
import static be.htkr.jnj.kalix.demo.view.GroupingName.PER_AGEGROUP;
import static be.htkr.jnj.kalix.demo.view.GroupingName.PER_COUNTRY;
import static be.htkr.jnj.kalix.demo.view.GroupingName.PER_GENDER;
import static be.htkr.jnj.kalix.demo.view.GroupingName.PER_MONTH;
import static be.htkr.jnj.kalix.demo.view.GroupingName.PER_QUARTER;
import static be.htkr.jnj.kalix.demo.view.GroupingName.PER_YEAR;
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
        String currentYear = GroupingName.timeStampToPeriodId(now, PER_YEAR);

        Collection<SingleLevelGroupedViewData> perYearResponse = getViewDataFor(PER_YEAR);
        verifyPerPeriod(perYearResponse, PER_YEAR, currentYear);

        String currentMonth = GroupingName.timeStampToPeriodId(now, PER_MONTH);
        Collection<SingleLevelGroupedViewData> perMonthResponse = getViewDataFor(PER_MONTH);
        verifyPerPeriod(perMonthResponse, PER_MONTH, currentMonth);

        String currentQuarter = GroupingName.timeStampToPeriodId(now, PER_QUARTER);
        Collection<SingleLevelGroupedViewData> perQuarterResponse = getViewDataFor(PER_QUARTER);
        verifyPerPeriod(perQuarterResponse, PER_QUARTER, currentQuarter);

        Collection<SingleLevelGroupedViewData> perAgeGroup = getViewDataFor(GroupingName.PER_AGEGROUP);
        var perGroup = perAgeGroup.stream().toList().get(0);
        assertThat(perGroup.groupName()).isEqualTo(PER_AGEGROUP.value);
        assertThat(perGroup.groupId()).isEqualTo(AgeGroup.MINUS_18.value);
        assertThat(perGroup.counters()).hasSize(1);
        assertThat(perGroup.counters().stream().filter(c -> c.status().equals("PROFILE_COMPLETE")).count()).isEqualTo(1);

        var perCountryGender = getViewDataFor(GroupingName.PER_GENDER, GroupingName.PER_COUNTRY);
        System.out.println(perCountryGender);
        var perCG = perCountryGender.stream().toList().get(0);
        assertThat(perCG.group1()).isEqualTo(PER_GENDER.value);
        assertThat(perCG.group2()).isEqualTo(PER_COUNTRY.value);
        assertThat(perCG.groupId()).isEqualTo("M_BE");
        assertThat(perCG.counters()).hasSize(1);
        assertThat(perCG.counters().stream().filter(c -> c.status().equals("PROFILE_COMPLETE")).count()).isEqualTo(1);
//move ageGroup
        eventsTopic.publish(new UserBusinessEvent.UserProfileCompleted(userId, new UserDetails("blue", "BE", "M", birthDate.minusYears(20)), Instant.now()), topicId);
        Thread.sleep(5000);
        perAgeGroup = getViewDataFor(GroupingName.PER_AGEGROUP);
        System.out.println("perAgeGroup after agegroup move: " + perAgeGroup);
        assertThat(perAgeGroup).hasSize(2);
        perGroup = perAgeGroup.stream().toList().get(0);
        assertThat(perGroup.groupName()).isEqualTo(PER_AGEGROUP.value);
        assertThat(perGroup.groupId()).isEqualTo(AgeGroup.MINUS_18.value);
        assertThat(perGroup.counters().get(0).count()).isEqualTo(0);
        assertThat(perGroup.groupName()).isEqualTo(PER_AGEGROUP.value);
        perGroup = perAgeGroup.stream().toList().get(1);
        assertThat(perGroup.groupId()).isEqualTo(AgeGroup._19_25.value);
        assertThat(perGroup.counters().get(0).count()).isEqualTo(1);
        assertThat(perGroup.counters().stream().filter(c -> c.status().equals("PROFILE_COMPLETE")).count()).isEqualTo(1);



    }

    private void verifyPerPeriod(Collection<SingleLevelGroupedViewData> groupedData, GroupingName periodName, String periodId) {
        var perPeriod = groupedData.stream().toList().get(0);
        assertThat(perPeriod.groupName()).isEqualTo(periodName.value);
        assertThat(perPeriod.groupId()).isEqualTo(periodId);
        assertThat(perPeriod.counters()).hasSize(2);
        assertThat(perPeriod.counters().stream().filter(c -> c.status().equals("REGISTERED")).count()).isEqualTo(1);
        assertThat(perPeriod.counters().stream().filter(c -> c.status().equals("PROFILE_COMPLETE")).count()).isEqualTo(1);
    }

    private Collection<SingleLevelGroupedViewData> getViewDataFor(GroupingName periodName) {
        return webClient.get().uri("/view/counters/{groupName}", Map.of("groupName", periodName.value))
                .retrieve().bodyToMono(SingleLevelGroupViewResponse.class).block().data();
    }
    private Collection<DualLevelGroupedViewData> getViewDataFor(GroupingName group1, GroupingName group2) {
        return webClient.get().uri("/view/counters/{groupName1}/{groupName2}", Map.of("groupName1", group1.value, "groupName2", group2.value))
                .retrieve().bodyToMono(DualLevelGroupViewResponse.class).block().data();
    }
}
