package be.htkr.jnj.kalix.demo.view.singlelevel;

import be.htkr.jnj.kalix.demo.entity.singlelevel.SingleLevelGroupedCounters;
import be.htkr.jnj.kalix.demo.entity.singlelevel.SingleLevelGroupingEntity;
import be.htkr.jnj.kalix.demo.entity.user.UserState;
import be.htkr.jnj.kalix.demo.view.StatusCounter;
import kalix.javasdk.annotations.Query;
import kalix.javasdk.annotations.Subscribe;
import kalix.javasdk.annotations.Table;
import kalix.javasdk.annotations.ViewId;
import kalix.javasdk.view.View;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ViewId("view_single_level_grouped")
@Table("single_level_grouped")
public class SingleLevelGroupingView extends View<SingleLevelGroupedViewData> {

    @Subscribe.ValueEntity(SingleLevelGroupingEntity.class)
    public UpdateEffect<SingleLevelGroupedViewData> onChange(SingleLevelGroupedCounters groupedCounters) {
        return effects()
                .updateState(new SingleLevelGroupedViewData(groupedCounters.groupName(), groupedCounters.groupId(), fromCounterMap(groupedCounters.counters()) ));
    }

    private List<StatusCounter> fromCounterMap(Map<UserState.Status, Integer> counters) {
        return counters.entrySet().stream()
                .map(e -> new StatusCounter(e.getKey().name(), e.getValue())).collect(Collectors.toList());
    }


    @GetMapping("/view/single/counters/{groupName}")
    @Query("SELECT * as data FROM single_level_grouped WHERE groupName = :groupName" )
    public SingleLevelGroupViewResponse getStatusPerGroup(@PathVariable("groupName") String groupName) {
        return null;
    }

}
