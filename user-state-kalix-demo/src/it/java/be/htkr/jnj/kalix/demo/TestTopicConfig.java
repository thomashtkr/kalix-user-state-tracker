package be.htkr.jnj.kalix.demo;

import kalix.javasdk.testkit.KalixTestKit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static be.htkr.jnj.kalix.demo.DemoConfig.STATUS_MOVEMENT_STREAM;
import static be.htkr.jnj.kalix.demo.DemoConfig.USER_BUSINESS_EVENTS_TOPIC;

@Configuration
public class TestTopicConfig {

    @Bean
    public KalixTestKit.Settings settings() {
        return KalixTestKit.Settings.DEFAULT.withAclEnabled()
                .withTopicIncomingMessages(USER_BUSINESS_EVENTS_TOPIC)
                .withTopicOutgoingMessages(STATUS_MOVEMENT_STREAM);
    }
}
