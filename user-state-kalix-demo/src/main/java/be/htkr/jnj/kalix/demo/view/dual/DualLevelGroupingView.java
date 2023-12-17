package be.htkr.jnj.kalix.demo.view.dual;

import be.htkr.jnj.kalix.demo.entity.dualevel.DualLevelGroupedCounters;
import be.htkr.jnj.kalix.demo.entity.dualevel.DualLevelGroupingEntity;
import be.htkr.jnj.kalix.demo.entity.user.UserState;
import be.htkr.jnj.kalix.demo.view.StatusCounter;
import kalix.javasdk.annotations.Query;
import kalix.javasdk.annotations.Subscribe;
import kalix.javasdk.annotations.Table;
import kalix.javasdk.annotations.ViewId;
import kalix.javasdk.view.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ViewId("view_dual_level_grouped")
@Table("dual_level_grouped")
public class DualLevelGroupingView extends View<DualLevelGroupedViewData> {

    private final Logger logger = LoggerFactory.getLogger(DualLevelGroupingView.class);

    @Subscribe.ValueEntity(DualLevelGroupingEntity.class)
    public UpdateEffect<DualLevelGroupedViewData> onChange(DualLevelGroupedCounters groupedCounters) {
        logger.info("updating view with {}", groupedCounters);
        return effects()
                .updateState(new DualLevelGroupedViewData(
                                groupedCounters.groupName1(),
                                groupedCounters.groupName2(),
                                groupedCounters.groupId(),
                                fromCounterMap(groupedCounters.counters()) ));
    }

    private List<StatusCounter> fromCounterMap(Map<UserState.Status, Integer> counters) {
        return counters.entrySet().stream()
                .map(e -> new StatusCounter(e.getKey().name(), e.getValue())).collect(Collectors.toList());
    }

    @GetMapping("/view/dual/counters/{group1}/{group2}")
    @Query("SELECT * as data FROM dual_level_grouped WHERE group1 = :group1 and group2 = :group2" )
    public DualLevelGroupViewResponse getStatusPerGroup(@PathVariable("group1") String group1, @PathVariable("group2") String group2) {
        return null;
    }


}
