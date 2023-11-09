package be.htkr.jnj.kalix.demo.event.simulation.actions.api;

import be.htkr.jnj.kalix.demo.event.simulation.actions.SimulationAction;
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
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RequestMapping("/api/simulate")
public class SimulationController extends Action {
    private final ComponentClient componentClient;

    private final Logger logger = LoggerFactory.getLogger(SimulationController.class);

    public SimulationController(ComponentClient componentClient) {
        this.componentClient = componentClient;
    }
    @PostMapping("/users/{count}")
    public Action.Effect<List<String>> simulateUsers(@PathVariable String count) {
        int numberOfUsers = Integer.parseInt(count);
        List<String> userIds = IntStream.range(0, numberOfUsers).boxed().map(i -> UUID.randomUUID().toString()).toList();

        var createUsers = userIds.stream().map(id -> SideEffect.of(createUser(id, id+"_name"),true)).collect(Collectors.toList());

        var stateChanges = simulateStateChanges(userIds);

        List<SideEffect> allSideEffects = new ArrayList<>(createUsers);
        allSideEffects.add(stateChanges);

        return effects().reply(userIds).addSideEffects(allSideEffects);
    }

    private SideEffect simulateStateChanges(List<String> userIds) {
        return SideEffect.of(componentClient.forAction().call(SimulationAction::simulateUserBehaviour)
                .params(userIds, 10));
    }


    

    private DeferredCall<Any, String > createUser(String id, String name) {
        return componentClient.forEventSourcedEntity(id)
                .call(UserEntity::register)
                .params(new UserCommand.RegisterUser(name));

    }
}
