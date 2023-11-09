package be.htkr.jnj.kalix.demo.action;

import be.htkr.jnj.kalix.demo.DemoConfig;
import be.htkr.jnj.kalix.demo.entity.statusperperiod.RegisterStatusMovementCommand;
import be.htkr.jnj.kalix.demo.entity.statusperperiod.StatusPerPeriodEntity;
import be.htkr.jnj.kalix.demo.entity.user.UserEntityStatusMovementEvent;
import be.htkr.jnj.kalix.demo.entity.user.UserStateEntity;
import com.google.protobuf.any.Any;
import kalix.javasdk.DeferredCall;
import kalix.javasdk.SideEffect;
import kalix.javasdk.action.Action;
import kalix.javasdk.annotations.Subscribe;
import kalix.javasdk.client.ComponentClient;
import kalix.spring.WebClientProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Subscribe.EventSourcedEntity(UserStateEntity.class)
public class StatusMovementToCounterDispatcher extends Action {

    private final Logger logger = LoggerFactory.getLogger(StatusMovementToCounterDispatcher.class);

    private final ComponentClient componentClient;
    private final WebClient webClient;

    public StatusMovementToCounterDispatcher(ComponentClient componentClient, WebClientProvider webClientProvider) {
        this.componentClient = componentClient;
        this.webClient = webClientProvider.webClientFor("user-state-tracker");
    }

    public Effect<String> dispatchMovement(UserEntityStatusMovementEvent event) {
        logger.info("dispatching {}", event);

        return effects().forward(groupByYearComponentCall(event));

        //return effects().asyncReply(groupByYearWebCall(event));
    }


    /**
     * Because the component-client does not yet have support for composite-keys, we call the entity by a web-request
     * https://github.com/lightbend/kalix-jvm-sdk/issues/1703
     */
    private CompletableFuture<String> groupByYearWebCall(UserEntityStatusMovementEvent event) {
        String year = String.valueOf(event.timestamp().atZone(ZoneId.systemDefault()).getYear());
        return webClient.post().uri("/counters/{periodName}/{periodId}/register-movement",
                            Map.of("periodName",DemoConfig.PeriodGroupingNames.PER_YEAR,
                                    "periodId", year))
                .bodyValue(new RegisterStatusMovementCommand(event.status(), event.movement()))
                .retrieve().bodyToMono(String.class).toFuture();


    }

    private DeferredCall<Any,String> groupByYearComponentCall(UserEntityStatusMovementEvent event) {
        String year = String.valueOf(event.timestamp().atZone(ZoneId.systemDefault()).getYear());
        String entityId = URLEncoder.encode(String.format("%s/%s", DemoConfig.PeriodGroupingNames.PER_YEAR, year), StandardCharsets.UTF_8);
        logger.info("calling entity StatusPerPeriodEntity: {} ", entityId );
        //return componentClient.forValueEntity(entityId)
        return componentClient.forValueEntity(DemoConfig.PeriodGroupingNames.PER_YEAR, year)
                .call(StatusPerPeriodEntity::registerMovement)
                .params(DemoConfig.PeriodGroupingNames.PER_YEAR, year, new RegisterStatusMovementCommand(event.status(), event.movement()));
    }
}
