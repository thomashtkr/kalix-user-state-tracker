package be.htkr.jnj.kalix.demo.entity.user;

import be.htkr.jnj.kalix.demo.entity.user.events.DemographicMovement;
import be.htkr.jnj.kalix.demo.entity.user.events.UserStatusMovement;
import be.htkr.jnj.kalix.demo.events.UserBusinessEvent;
import be.htkr.jnj.kalix.demo.events.model.User;
import be.htkr.jnj.kalix.demo.events.model.UserDetails;
import kalix.javasdk.testkit.EventSourcedTestKit;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static be.htkr.jnj.kalix.demo.entity.user.AgeGroup._19_25;
import static be.htkr.jnj.kalix.demo.entity.user.AgeGroup._26_35;
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
            testKit.call(e -> e.updateUserState(new UpdateUserStateCommand(new UserBusinessEvent.UserRegistered(userId, new User(userId, "name", "email"), Instant.now()))));
            assertThat(testKit.getAllEvents()).hasSize(1);

            testKit.call(e -> e.updateUserState(new UpdateUserStateCommand(new UserBusinessEvent.UserVerified(userId, "email", Instant.now()))));
            assertThat(testKit.getState().currentStatus()).isEqualTo(UserState.Status.EMAIL_VERIFIED);
            assertThat(testKit.getState().previousStatus()).isEqualTo(UserState.Status.REGISTERED);

            assertThat(testKit.getAllEvents().size()).isEqualTo(3);
            List<UserStatusMovement> statusMovements = testKit.getAllEvents().stream().map(UserStatusMovement.class::cast).toList();
            assertThat(statusMovements.get(0).status()).isEqualTo(UserState.Status.REGISTERED);
            assertThat(statusMovements.get(0).movement()).isEqualTo(1);
            assertThat(statusMovements.get(1).status()).isEqualTo(UserState.Status.EMAIL_VERIFIED);
            assertThat(statusMovements.get(1).status()).isEqualTo(UserState.Status.EMAIL_VERIFIED);
            assertThat(statusMovements.get(2).status()).isEqualTo(UserState.Status.REGISTERED);
            assertThat(statusMovements.get(2).movement()).isEqualTo(-1);
        }
    }

    @Test
    void testCompleteProfile() {
        var testKit = EventSourcedTestKit.of(UserStateEntity::new);
        {
            var userId = UUID.randomUUID().toString();
            var now = LocalDate.now();
            var birthDate = createDate(now.getYear() - 30, now.getMonth().getValue(), now.getDayOfMonth());
            var completed = new UserBusinessEvent.UserProfileCompleted(userId, new UserDetails("blue", "be", "F", birthDate), Instant.now());
            testKit.call(e -> e.updateUserState(new UpdateUserStateCommand(completed)));
            assertThat(testKit.getState().currentStatus()).isEqualTo(UserState.Status.PROFILE_COMPLETE);
            assertThat(testKit.getState().currentDemographic().ageGroup()).isEqualTo(_26_35);
            List<UserStatusMovement> statusMovements = testKit.getAllEvents().stream().filter(e -> e instanceof UserStatusMovement).map(UserStatusMovement.class::cast).toList();
            List<DemographicMovement> demographicMovements = testKit.getAllEvents().stream().filter(e -> e instanceof DemographicMovement).map(DemographicMovement.class::cast).toList();
            assertThat(statusMovements).hasSize(1);
            assertThat(demographicMovements).hasSize(1);
            assertThat(statusMovements.get(0).status()).isEqualTo(UserState.Status.PROFILE_COMPLETE);
            assertThat(statusMovements.get(0).movement()).isEqualTo(1);
            assertThat(demographicMovements.get(0).demographic().ageGroup()).isEqualTo(_26_35);
            assertThat(demographicMovements.get(0).demographic().country()).isEqualTo("be");
            assertThat(demographicMovements.get(0).demographic().gender()).isEqualTo("F");
            assertThat(demographicMovements.get(0).demographic().favoriteColor()).isEqualTo("blue");
            assertThat(demographicMovements.get(0).movement()).isEqualTo(1);

        }
    }

    @Test
    void testUpdateUserAgeGroup() {
        var testKit = EventSourcedTestKit.of(UserStateEntity::new);
        {
            var userId = UUID.randomUUID().toString();
            var now = LocalDate.now();
            var birthDate = createDate(now.getYear() - 30, now.getMonth().getValue(), now.getDayOfMonth());
            var completed = new UserBusinessEvent.UserProfileCompleted(userId, new UserDetails("blue", "be", "F", birthDate), Instant.now());
            testKit.call(e -> e.updateUserState(new UpdateUserStateCommand(completed)));
            assertThat(testKit.getState().currentStatus()).isEqualTo(UserState.Status.PROFILE_COMPLETE);
            assertThat(testKit.getState().currentDemographic().ageGroup()).isEqualTo(_26_35);
            testKit.call(e -> e.updateAgeGroup(new UpdateAgeGroupCommand()));

            List<UserStatusMovement> statusMovements = testKit.getAllEvents().stream().filter(e -> e instanceof UserStatusMovement).map(UserStatusMovement.class::cast).toList();
            assertThat(statusMovements.get(0).status()).isEqualTo(UserState.Status.PROFILE_COMPLETE);
            assertThat(statusMovements.get(0).movement()).isEqualTo(1);
            assertThat(statusMovements).hasSize(1);
            List<DemographicMovement> demographicMovements = testKit.getAllEvents().stream().filter(e -> e instanceof DemographicMovement).map(DemographicMovement.class::cast).toList();
            assertThat(demographicMovements).hasSize(1);
            assertThat(demographicMovements.get(0).status()).isEqualTo(UserState.Status.PROFILE_COMPLETE);
            assertThat(demographicMovements.get(0).demographic().ageGroup()).isEqualTo(_26_35);
            assertThat(demographicMovements.get(0).movement()).isEqualTo(1);

            var completedAgain = new UserBusinessEvent.UserProfileCompleted(userId, new UserDetails("blue", "be", "F", birthDate.plusYears(10)), Instant.now());
            testKit.call(e -> e.updateUserState(new UpdateUserStateCommand(completedAgain)));
            assertThat(testKit.getState().currentDemographic().ageGroup()).isEqualTo(_19_25);

        }
    }

    @Test
    void testDeduplicateCommands() {
        var testKit = EventSourcedTestKit.of(UserStateEntity::new);
        {
            var userId = UUID.randomUUID().toString();
            testKit.call(e -> e.updateUserState(new UpdateUserStateCommand(new UserBusinessEvent.UserRegistered(userId, new User(userId, "name", "email"), Instant.now()))));
            assertThat(testKit.getAllEvents()).hasSize(1);

            testKit.call(e -> e.updateUserState(new UpdateUserStateCommand(new UserBusinessEvent.UserVerified(userId, "email", Instant.now()))));
            testKit.call(e -> e.updateUserState(new UpdateUserStateCommand(new UserBusinessEvent.UserVerified(userId, "email", Instant.now()))));
            testKit.call(e -> e.updateUserState(new UpdateUserStateCommand(new UserBusinessEvent.UserVerified(userId, "email", Instant.now()))));
            testKit.call(e -> e.updateUserState(new UpdateUserStateCommand(new UserBusinessEvent.UserVerified(userId, "email", Instant.now()))));
            assertThat(testKit.getAllEvents().size()).isEqualTo(3);
            assertThat(testKit.getState().currentStatus()).isEqualTo(UserState.Status.EMAIL_VERIFIED);
            assertThat(testKit.getState().previousStatus()).isEqualTo(UserState.Status.REGISTERED);

        }
    }




    private LocalDate createDate(int year, int month, int day) {
        return LocalDate.parse(String.format("%d-%02d-%02d", year, month, day));
    }
}
