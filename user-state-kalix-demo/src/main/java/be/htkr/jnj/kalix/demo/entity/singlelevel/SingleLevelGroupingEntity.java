package be.htkr.jnj.kalix.demo.entity.singlelevel;

import kalix.javasdk.annotations.Id;
import kalix.javasdk.annotations.TypeId;
import kalix.javasdk.valueentity.ValueEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;

@TypeId("status-per-period")
@Id({"groupName", "groupId"})
@RequestMapping("/counters")
public class SingleLevelGroupingEntity extends ValueEntity<SingleLevelGroupedCounters> {

    private final Logger logger = LoggerFactory.getLogger(SingleLevelGroupingEntity.class);

    @Override
    public SingleLevelGroupedCounters emptyState() { return new SingleLevelGroupedCounters(null, null, new HashMap<>()); }


    @PostMapping("/{groupName}/{groupId}/register-movement")
    public Effect<String> registerMovement(@PathVariable("groupName") String periodName, @PathVariable("groupId") String periodId, @RequestBody RegisterStatusMovementCommand command) {
        logger.info("movement for groupName {} groupId {}", periodName, periodId);
        SingleLevelGroupedCounters state = currentState();
        if(state.groupName() == null) {
            state = state.setGroupData(periodName, periodId);
        }
        return effects()
                .updateState(state.count(command.status(), command.movement()))
                .thenReply("OK");
    }

}
