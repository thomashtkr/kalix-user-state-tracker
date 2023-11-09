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

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
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
            return IntStream.range(0, number).mapToObj(i -> {
                var callSupplier = ENTITY_CALLS.get(random.nextInt(ENTITY_CALLS.size()));
                return callSupplier.apply(userId);
            });
        }).toList();

        return effects().reply("OK")
                .addSideEffects(effects);
    }

    private DeferredCall<Any, String> storeProfile(String userId) {
        return componentClient.forEventSourcedEntity(userId)
                .call(UserEntity::storeProfile)
                .params(new UserCommand.StoreUserProfile(userId+"_email", userId+"_color", userId+"_country", userId+"_gender"));
    }

    private DeferredCall<Any, String> confirmGdpr(String userId) {
        return componentClient.forEventSourcedEntity(userId)
                .call(UserEntity::confirmGdpr)
                .params(new UserCommand.ConfirmGdpr(UUID.randomUUID()));
    }

    private DeferredCall<Any, String> verifyEmail(String userId) {
        return componentClient.forEventSourcedEntity(userId)
                .call(UserEntity::verifyEmail)
                .params(new UserCommand.VerifyEmail(userId+"_email"));
    }

}
