package be.htkr.jnj.kalix.demo;

import be.htkr.jnj.kalix.demo.entity.user.UserState;
import be.htkr.jnj.kalix.demo.events.UserBusinessEvent;
import be.htkr.jnj.kalix.demo.events.UserStatusMovement;
import be.htkr.jnj.kalix.demo.events.model.User;
import be.htkr.jnj.kalix.demo.events.model.UserDetails;
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
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = Main.class)
@ExtendWith(MockitoExtension.class)
@Import(TestTopicConfig.class)
public class ConsumeIntegrationTest extends KalixIntegrationTestKitSupport {

    @Autowired
    private KalixTestKit kalixTestKit;


    private EventingTestKit.Topic eventsTopic;

    private EventingTestKit.Topic movementsStream;

    @Autowired
    private WebClient webClient;


    @BeforeAll
    public void beforeAll() throws IOException {
        eventsTopic = kalixTestKit.getTopic(DemoConfig.USER_BUSINESS_EVENTS_TOPIC);
        movementsStream = kalixTestKit.getTopic(DemoConfig.STATUS_MOVEMENT_STREAM);

    }


    @Test
    public void verifyCounterEventSourcedPublishToTopic() throws Exception {
        var topicId = "user-events";
        var userId = UUID.randomUUID().toString();
        var registeredUser = new UserBusinessEvent.UserRegistered(userId, new User(userId, "name", "email"), Instant.now());
        eventsTopic.publish(registeredUser, topicId);
        eventsTopic.publish(new UserBusinessEvent.UserProfileCompleted(userId, new UserDetails("blue", "BE", "M"), Instant.now()), topicId);
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
    }
}
