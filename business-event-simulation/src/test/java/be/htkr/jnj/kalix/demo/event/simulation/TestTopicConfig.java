package be.htkr.jnj.kalix.demo.event.simulation;

import kalix.javasdk.testkit.KalixTestKit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestTopicConfig {

    @Bean
    public KalixTestKit.Settings settings() {
        return KalixTestKit.Settings.DEFAULT
                .withTopicIncomingMessages("user-events");
    }
}
