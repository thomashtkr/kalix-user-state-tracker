package be.htkr.jnj.kalix.demo.entity.statusperperiod;

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
@Id({"periodName", "periodId"})
@RequestMapping("/counters")
public class StatusPerPeriodEntity extends ValueEntity<StatusPerPeriod> {

    private final Logger logger = LoggerFactory.getLogger(StatusPerPeriodEntity.class);



    @Override
    public StatusPerPeriod emptyState() { return new StatusPerPeriod(null, null, new HashMap<>()); }


    @PostMapping("/{periodName}/{periodId}/register-movement")
    public Effect<String> registerMovement(@PathVariable("periodName") String periodName, @PathVariable("periodId") String periodId, @RequestBody RegisterStatusMovementCommand command) {
        logger.info("registering movement for {}", commandContext().entityId());
        logger.info("periodName {} periodId {}", periodName, periodId);
        StatusPerPeriod state = currentState();
        if(state.periodName() == null) {
            state = state.setGroupData(periodName, periodId);
        }
        return effects()
                .updateState(state.count(command.status(), command.movement()))
                .thenReply("OK");
    }

}
