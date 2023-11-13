package be.htkr.jnj.kalix.demo.entity.user;

import be.htkr.jnj.kalix.demo.events.UserBusinessEvent;
import be.htkr.jnj.kalix.demo.events.model.User;
import be.htkr.jnj.kalix.demo.events.model.UserDetails;
import kalix.javasdk.testkit.EventSourcedTestKit;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoField;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class UserStateEntityTest {

    @Test
    void testInitialState() {
        var testKit = EventSourcedTestKit.of(UserStateEntity::new);
        {
            assertThat(testKit.getState().currentStatus()).isNull();
        }
    }

    @Test
    void testUpdateState() {
        var testKit = EventSourcedTestKit.of(UserStateEntity::new);
        {
            var userId = UUID.randomUUID().toString();
            testKit.call(e -> e.updateStatus(new UpdateUserStateCommand(new UserBusinessEvent.UserRegistered(userId, new User(userId, "name", "email"), Instant.now()))));
            assertThat(testKit.getAllEvents()).hasSize(1);

            testKit.call(e -> e.updateStatus(new UpdateUserStateCommand(new UserBusinessEvent.UserVerified(userId, "email", Instant.now()))));
            assertThat(testKit.getState().currentStatus()).isEqualTo(UserState.Status.EMAIL_VERIFIED);
            assertThat(testKit.getState().previousStatus()).isEqualTo(UserState.Status.REGISTERED);

            assertThat(testKit.getAllEvents().size()).isEqualTo(3);
            assertThat(testKit.getAllEvents().get(0).status()).isEqualTo(UserState.Status.REGISTERED);
            assertThat(testKit.getAllEvents().get(0).movement()).isEqualTo(1);
            assertThat(testKit.getAllEvents().get(1).status()).isEqualTo(UserState.Status.EMAIL_VERIFIED);
            assertThat(testKit.getAllEvents().get(1).status()).isEqualTo(UserState.Status.EMAIL_VERIFIED);
            assertThat(testKit.getAllEvents().get(2).status()).isEqualTo(UserState.Status.REGISTERED);
            assertThat(testKit.getAllEvents().get(2).movement()).isEqualTo(-1);
        }
    }

    @Test
    void testCompleteProfile() {
        var testKit = EventSourcedTestKit.of(UserStateEntity::new);
        {
            var userId = UUID.randomUUID().toString();
            var now = LocalDate.now();
            var bithDate = createDate(now.getYear() - 30, now.getMonth().getValue(), now.getDayOfMonth());
            var completed = new UserBusinessEvent.UserProfileCompleted(userId, new UserDetails("blue", "be", "F", bithDate), Instant.now());
            testKit.call(e -> e.updateStatus(new UpdateUserStateCommand(completed)));
            assertThat(testKit.getState().currentStatus()).isEqualTo(UserState.Status.PROFILE_COMPLETE);
            assertThat(testKit.getState().demographic().ageGroup()).isEqualTo(UserDemographic.AgeGroup._26_35);
            assertThat(testKit.getAllEvents().get(0).status()).isEqualTo(UserState.Status.PROFILE_COMPLETE);
            assertThat(testKit.getAllEvents().get(0).movement()).isEqualTo(1);

        }
    }

    private LocalDate createDate(int year, int month, int day) {
        return LocalDate.parse(String.format("%d-%02d-%02d", year, month, day));
    }
}
