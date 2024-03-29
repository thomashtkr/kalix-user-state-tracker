package be.htkr.jnj.kalix.demo;

import be.htkr.jnj.kalix.demo.entity.dualevel.DualLevelGroupedCounters;
import be.htkr.jnj.kalix.demo.entity.singlelevel.SingleLevelGroupedCounters;
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
import be.htkr.jnj.kalix.demo.view.users.UserData;
import be.htkr.jnj.kalix.demo.view.users.UserDataResponse;
import kalix.javasdk.testkit.EventingTestKit;
import kalix.javasdk.testkit.KalixTestKit;
import kalix.spring.testkit.KalixIntegrationTestKitSupport;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static be.htkr.jnj.kalix.demo.DemoConfig.STATUS_MOVEMENT_STREAM;
import static be.htkr.jnj.kalix.demo.view.GroupingName.PER_AGEGROUP;
import static be.htkr.jnj.kalix.demo.view.GroupingName.PER_COUNTRY;
import static be.htkr.jnj.kalix.demo.view.GroupingName.PER_GENDER;
import static be.htkr.jnj.kalix.demo.view.GroupingName.PER_MONTH;
import static be.htkr.jnj.kalix.demo.view.GroupingName.PER_QUARTER;
import static be.htkr.jnj.kalix.demo.view.GroupingName.PER_YEAR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

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
        Awaitility.setDefaultPollInterval(1, TimeUnit.SECONDS);
        Awaitility.setDefaultPollDelay(Duration.ZERO);
        Awaitility.setDefaultTimeout(Duration.ONE_MINUTE);
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
        UserStatusMovement reg = movementsStream.expectOneTyped(UserStatusMovement.class, java.time.Duration.of(5, ChronoUnit.SECONDS)).getPayload();
        assertThat(reg.movement()).isEqualTo(1);
        assertThat(reg.status()).isEqualTo(UserState.Status.REGISTERED.name());

        UserStatusMovement completed = movementsStream.expectOneTyped(UserStatusMovement.class).getPayload();
        assertThat(completed.movement()).isEqualTo(1);
        assertThat(completed.status()).isEqualTo(UserState.Status.PROFILE_COMPLETE.name());

        UserStatusMovement unRegistered = movementsStream.expectOneTyped(UserStatusMovement.class).getPayload();
        assertThat(unRegistered.movement()).isEqualTo(-1);
        assertThat(unRegistered.status()).isEqualTo(UserState.Status.REGISTERED.name());

        assertThat(movementsStream.clear()).isEmpty();


        Instant now = Instant.now();
        String currentYear = GroupingName.timeStampToPeriodId(now, PER_YEAR);

        await().until(() -> !getViewDataFor(PER_YEAR).isEmpty());

        Collection<SingleLevelGroupedViewData> perYearResponse = getViewDataFor(PER_YEAR);
        verifyPerPeriod(perYearResponse, PER_YEAR, currentYear);
        SingleLevelGroupedCounters yearCounters = getEntityDataFor(PER_YEAR, currentYear);
        System.out.println("entityResponse " + yearCounters);
        assertThat(yearCounters.groupId()).isEqualTo(currentYear);
        assertThat(yearCounters.counters()).satisfies(counters -> assertThat(counters.get(UserState.Status.PROFILE_COMPLETE)).isEqualTo(1));
        assertThat(yearCounters.counters()).satisfies(counters -> assertThat(counters.get(UserState.Status.REGISTERED)).isEqualTo(0));

        String currentMonth = GroupingName.timeStampToPeriodId(now, PER_MONTH);
        Collection<SingleLevelGroupedViewData> perMonthResponse = getViewDataFor(PER_MONTH);
        verifyPerPeriod(perMonthResponse, PER_MONTH, currentMonth);

        String currentQuarter = GroupingName.timeStampToPeriodId(now, PER_QUARTER);
        Collection<SingleLevelGroupedViewData> perQuarterResponse = getViewDataFor(PER_QUARTER);
        verifyPerPeriod(perQuarterResponse, PER_QUARTER, currentQuarter);

        Collection<SingleLevelGroupedViewData> perAgeGroup = getViewDataFor(GroupingName.PER_AGEGROUP);
        System.out.println("perAgeGroup before agegroup move: " + perAgeGroup);
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
        var dualGroupEntityCounters = getEntityDataFor(PER_GENDER, PER_COUNTRY, "M_BE");
        assertThat(dualGroupEntityCounters.groupName1()).isEqualTo(PER_GENDER.value);
        assertThat(dualGroupEntityCounters.groupName2()).isEqualTo(PER_COUNTRY.value);
        assertThat(dualGroupEntityCounters.groupId()).isEqualTo("M_BE");
        assertThat(dualGroupEntityCounters.counters()).hasSize(1);
        assertThat(dualGroupEntityCounters.counters()).satisfies(m -> assertThat(m.get(UserState.Status.PROFILE_COMPLETE)).isEqualTo(1));


//move ageGroup
        eventsTopic.publish(new UserBusinessEvent.UserProfileCompleted(userId, new UserDetails("blue", "BE", "M", birthDate.minusYears(20)), Instant.now()), topicId);
        await().until(() -> getViewDataFor(PER_AGEGROUP).size() == 2);
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

        Collection<UserData> users = getUsers();
        assertThat(users).hasSize(1);
        assertThat(users.iterator().next().userId()).isEqualTo(userId);


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
        return webClient.get().uri("/view/single/counters/{groupName}", Map.of("groupName", periodName.value))
                .retrieve().bodyToMono(SingleLevelGroupViewResponse.class).block().data();
    }

    private SingleLevelGroupedCounters getEntityDataFor(GroupingName groupingName, String groupId) {
        return webClient.get().uri("/view/single/counters/{groupName}/{groupId}", Map.of("groupName", groupingName.value, "groupId", groupId))
                .retrieve().bodyToMono(SingleLevelGroupedCounters.class).block();
    }

    private DualLevelGroupedCounters getEntityDataFor(GroupingName group1, GroupingName group2, String groupId) {
        return webClient.get().uri("/view/dual/counters/{group1}/{group2}/{groupId}", Map.of("group1", group1.value, "group2", group2.value, "groupId", groupId))
                .retrieve().bodyToMono(DualLevelGroupedCounters.class).block();
    }
    private Collection<DualLevelGroupedViewData> getViewDataFor(GroupingName group1, GroupingName group2) {
        return webClient.get().uri("/view/dual/counters/{groupName1}/{groupName2}", Map.of("groupName1", group1.value, "groupName2", group2.value))
                .retrieve().bodyToMono(DualLevelGroupViewResponse.class).block().data();
    }

    private Collection<UserData> getUsers() {
        return webClient.get().uri("/view/users")
                .retrieve().bodyToMono(UserDataResponse.class).block().users();
    }
}
