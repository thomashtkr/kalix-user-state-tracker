package be.htkr.jnj.kalix.demo.view;

import java.util.List;

public record SingleLevelGroupedViewData(String groupName, String groupId, List<StatusCounter> counters) {

    public SingleLevelGroupedViewData count(String status, int movement) {
        //counters.merge(status, movement, Integer::sum);
        return new SingleLevelGroupedViewData(groupName(), groupId(), counters);
    }

    public SingleLevelGroupedViewData setGroupData(String periodName, String periodId) {
        return new SingleLevelGroupedViewData(periodName, periodId, counters());
    }
}
