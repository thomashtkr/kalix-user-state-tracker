package be.htkr.jnj.kalix.demo.entity.singlelevel;

import be.htkr.jnj.kalix.demo.entity.user.UserState;

import java.util.Map;

public record SingleLevelGroupedCounters(String groupName, String groupId, Map<UserState.Status, Integer> counters) {

    public SingleLevelGroupedCounters count(UserState.Status status, int movement) {
        counters.merge(status, movement, Integer::sum);
        return new SingleLevelGroupedCounters(groupName(), groupId(), counters);
    }

    public SingleLevelGroupedCounters setGroupData(String periodName, String periodId) {
        return new SingleLevelGroupedCounters(periodName, periodId, counters());
    }
}
