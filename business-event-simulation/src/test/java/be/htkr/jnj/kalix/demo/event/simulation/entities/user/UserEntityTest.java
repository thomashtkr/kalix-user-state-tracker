package be.htkr.jnj.kalix.demo.event.simulation.entities.user;

import kalix.javasdk.testkit.EventSourcedTestKit;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


public class UserEntityTest {
    @Test
    void testInitialState() {
        var testKit = EventSourcedTestKit.of(UserEntity::new);
        {
            assertThat(testKit.getState().id()).isNull();
        }

    }

    @Test
    void testRegisterUser() {
        var testKit = EventSourcedTestKit.of(UserEntity::new);
        {
            var result = testKit.call(e -> e.register(new UserCommand.RegisterUser( "john")));
            assertThat(result.getReply()).isEqualTo("OK");

            var registeredEvent = result.getNextEventOfType(UserEvent.UserRegistered.class);
            assertThat(registeredEvent.userId()).isNotEmpty();

            assertThat(registeredEvent.name()).isEqualTo("john");

            var error = testKit.call(e -> e.register(new UserCommand.RegisterUser( "john")));
            assertThat(error.getError()).contains("already registered");

            assertThat(testKit.getState().id()).isNotEmpty();
            assertThat(testKit.getState().name()).isNotEmpty();
            assertThat(testKit.getState().favoriteColor()).isNull();
            assertThat(testKit.getState().confirmedTandC()).isNull();
            assertThat(testKit.getState().email()).isNull();

        }
    }

    @Test
    void testUserProfile() {
        var testKit = EventSourcedTestKit.of(UserEntity::new);
        {
            LocalDate birtdate = LocalDate.now();
            var result = testKit.call(e -> e.storeProfile(new UserCommand.StoreUserProfile( "john@gmail.com", "blue", "BE", "M", birtdate)));
            assertThat(result.getReply()).isEqualTo("OK");
            assertThat(testKit.getState().favoriteColor()).isEqualTo("blue");
            assertThat(testKit.getState().birthDate()).isEqualTo(birtdate);
        }
    }

    @Test
    void testGdprConfirmation() {
        var testKit = EventSourcedTestKit.of(UserEntity::new);
        var tc = UUID.randomUUID();
        {
            var result = testKit.call(e -> e.confirmGdpr(new UserCommand.ConfirmGdpr(tc)));
            assertThat(result.getReply()).isEqualTo("OK");
            assertThat(testKit.getState().confirmedTandC()).isEqualTo(tc);
            assertThat(testKit.getState().favoriteColor()).isNull();
            assertThat(testKit.getState().name()).isNull();
        }
    }

    @Test
    void testVerifyEmail() {
        var testKit = EventSourcedTestKit.of(UserEntity::new);
        var email = "verified@mail.com";
        {
            var result = testKit.call(e -> e.verifyEmail(new UserCommand.VerifyEmail(email)));
            assertThat(result.getReply()).isEqualTo("OK");
            assertThat(testKit.getState().email()).isEqualTo(email);
            assertThat(testKit.getState().favoriteColor()).isNull();
            assertThat(testKit.getState().name()).isNull();
        }
    }
}
