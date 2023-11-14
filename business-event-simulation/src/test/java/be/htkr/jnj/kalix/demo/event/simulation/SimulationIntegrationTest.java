package be.htkr.jnj.kalix.demo.event.simulation;

import kalix.javasdk.testkit.EventingTestKit;
import kalix.javasdk.testkit.KalixTestKit;
import kalix.spring.testkit.KalixIntegrationTestKitSupport;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.List;

@SpringBootTest(classes = Main.class)
@ExtendWith(MockitoExtension.class)
@Import(TestTopicConfig.class)
public class SimulationIntegrationTest extends KalixIntegrationTestKitSupport {


    @Autowired
    private KalixTestKit kalixTestKit;
    private EventingTestKit.Topic eventsTopic;

    @Autowired
    private WebClient webClient;

    @BeforeAll
    public void beforeAll() throws IOException {
        eventsTopic = kalixTestKit.getTopic("user-events");
    }

    @Test
    public void testUserEventSimulation() throws Exception {
        int numnerOfUsers = 50;
        List response = webClient.post().uri("/api/simulate/users/" + numnerOfUsers)
                .retrieve()
                .bodyToMono(List.class)
                .block();
        System.out.println("response " + response);
        Assertions.assertThat(response).hasSize(numnerOfUsers);
        



    }


}
