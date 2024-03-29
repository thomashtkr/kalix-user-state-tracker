package be.htkr.jnj.kalix.demo.event.simulation.actions;

import be.htkr.jnj.kalix.demo.event.simulation.entities.user.UserCommand;
import be.htkr.jnj.kalix.demo.event.simulation.entities.user.UserEntity;
import com.google.protobuf.any.Any;
import kalix.javasdk.DeferredCall;
import kalix.javasdk.SideEffect;
import kalix.javasdk.action.Action;
import kalix.javasdk.client.ComponentClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * simulates random changes for Users
 */
@RequestMapping("/internal")
public class SimulationAction extends Action {

    private final ComponentClient componentClient;
    private final Logger logger = LoggerFactory.getLogger(SimulationAction.class);

    private final Random random = new Random(System.currentTimeMillis());

    private final List<Function<String, SideEffect>> ENTITY_CALLS = List.of(
            id -> SideEffect.of(verifyEmail(id)),
            id -> SideEffect.of(storeProfile(id)),
            id -> SideEffect.of(confirmGdpr(id)));

    public SimulationAction(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }

    @PostMapping("/simulate-behaviour/{number}")
    public Effect<String> simulateUserBehaviour(@RequestBody List<String> userIds, @PathVariable("number") Integer number) {
        logger.info("creating {} statechanges for {} users", number, userIds);
        List<SideEffect> effects = userIds.stream().flatMap(userId -> {
            return IntStream.range(1, number).mapToObj(i -> {
                var callSupplier = ENTITY_CALLS.get(random.nextInt(ENTITY_CALLS.size()));
                return callSupplier.apply(userId);
            });
        }).toList();

        return effects().reply("OK")
                .addSideEffects(effects);
    }

    private DeferredCall<Any, String> storeProfile(String userId) {
        logger.info(" updating profile for {}", userId);
        int month = random.nextInt(11) + 1;
        int oldest = 1950;
        int youngest = 2020;
        int year = random.nextInt(youngest-oldest) + oldest;
        int day = random.nextInt(26) + 1;
        LocalDate birthdate = createDate(year, month, day);
        return componentClient.forEventSourcedEntity(userId)
                .call(UserEntity::storeProfile)
                .params(new UserCommand.StoreUserProfile(userId+"_email", userId+"_color", userId+"_country", userId+"_gender", birthdate));
    }
    private LocalDate createDate(int year, int month, int day) {
        return LocalDate.parse(String.format("%d-%02d-%02d", year, month, day));
    }

    private DeferredCall<Any, String> confirmGdpr(String userId) {
        logger.info(" confirmGdpr for {}", userId);
        return componentClient.forEventSourcedEntity(userId)
                .call(UserEntity::confirmGdpr)
                .params(new UserCommand.ConfirmGdpr(UUID.randomUUID()));
    }

    private DeferredCall<Any, String> verifyEmail(String userId) {
        logger.info(" verifyEmail for {}", userId);
        return componentClient.forEventSourcedEntity(userId)
                .call(UserEntity::verifyEmail)
                .params(new UserCommand.VerifyEmail(userId+"_email"));
    }

}
