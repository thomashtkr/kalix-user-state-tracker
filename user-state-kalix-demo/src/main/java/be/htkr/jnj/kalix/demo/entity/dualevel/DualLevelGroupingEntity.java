package be.htkr.jnj.kalix.demo.entity.dualevel;

import be.htkr.jnj.kalix.demo.entity.singlelevel.RegisterStatusMovementCommand;
import be.htkr.jnj.kalix.demo.entity.singlelevel.SingleLevelGroupedCounters;
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

@TypeId("dual-level-grouped-status")
@Id({"group1", "group2", "groupId"})
@RequestMapping("/group")
public class DualLevelGroupingEntity extends ValueEntity<DualLevelGroupedCounters> {

    private final Logger logger = LoggerFactory.getLogger(DualLevelGroupingEntity.class);

    @Override
    public DualLevelGroupedCounters emptyState() {
        return new DualLevelGroupedCounters(null, null, null, new HashMap<>());
    }

    @PostMapping("/{group1}/{group2}/{groupId}/register-movement")
    public Effect<String> registerMovement(@PathVariable("group1") String group1,
                                           @PathVariable("group2") String group2,
                                           @PathVariable("groupId") String groupId,
                                           @RequestBody RegisterStatusMovementCommand command) {

        logger.info("movement for groups {}/{} groupId {}", group1, group2, groupId);
        DualLevelGroupedCounters state = currentState();
        if(state.groupName1() == null && state.groupName2() == null) {
            state = state.setGroupData(group1, group2, groupId);
        }
        return effects()
                .updateState(state.count(command.status(), command.movement()))
                .thenReply("OK");
    }

    @GetMapping("/{group1}/{group2}/{groupId}")
    public Effect<DualLevelGroupedCounters> getCurrentState() {
        return effects().reply(currentState());
    }
}
