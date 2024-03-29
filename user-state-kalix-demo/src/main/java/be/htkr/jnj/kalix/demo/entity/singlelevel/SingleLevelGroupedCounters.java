package be.htkr.jnj.kalix.demo.entity.singlelevel;

import be.htkr.jnj.kalix.demo.entity.user.UserState;

import java.util.Map;

public record SingleLevelGroupedCounters(String groupName, String groupId, Map<UserState.Status, Integer> counters) {
    public SingleLevelGroupedCounters count(UserState.Status status, int movement) {
        int newValue = counters.merge(status, movement, Integer::sum);
        if(newValue < 0) {
            counters.put(status,0);
        }
        return new SingleLevelGroupedCounters(groupName(), groupId(), counters);
    }

    public SingleLevelGroupedCounters setGroupData(String groupName, String groupId) {
        return new SingleLevelGroupedCounters(groupName, groupId, counters());
    }
}
