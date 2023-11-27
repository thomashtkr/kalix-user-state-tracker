package be.htkr.jnj.kalix.demo.entity.singlelevel;

import kalix.javasdk.annotations.Id;
import kalix.javasdk.annotations.TypeId;
import kalix.javasdk.valueentity.ValueEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;

@TypeId("single-level-grouped-status")
@Id({"groupName", "groupId"})
public class SingleLevelGroupingEntity extends ValueEntity<SingleLevelGroupedCounters> {

    private final Logger logger = LoggerFactory.getLogger(SingleLevelGroupingEntity.class);

    @Override
    public SingleLevelGroupedCounters emptyState() { return new SingleLevelGroupedCounters(null, null, new HashMap<>()); }


    @PostMapping("/group/{groupName}/{groupId}/register-movement")
    public Effect<String> registerMovement(@PathVariable("groupName") String groupName, @PathVariable("groupId") String groupId,
                                           @RequestBody RegisterStatusMovementCommand command) {
        logger.info("movement {} for groupName {} groupId {}",command.movement(), groupName, groupId);
        SingleLevelGroupedCounters state = currentState();
        if(state.groupName() == null) {
            state = state.setGroupData(groupName, groupId);
        }
        return effects()
                .updateState(state.count(command.status(), command.movement()))
                .thenReply("OK");
    }

    @GetMapping("/view/single/counters/{groupName}/{groupId}")
    public Effect<SingleLevelGroupedCounters> getCurrentState() {
        return effects().reply(currentState());
    }

}
