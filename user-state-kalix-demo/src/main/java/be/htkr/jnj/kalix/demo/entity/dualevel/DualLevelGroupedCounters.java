package be.htkr.jnj.kalix.demo.entity.dualevel;

import be.htkr.jnj.kalix.demo.entity.user.UserState;

import java.util.Map;

public record DualLevelGroupedCounters(String groupName1, String groupName2, String groupId, Map<UserState.Status, Integer> counters) {

    public DualLevelGroupedCounters count(UserState.Status status, int movement) {
        int newValue = counters.merge(status, movement, Integer::sum);
        if(newValue < 0) {
            counters.put(status,0);
        }
        return new DualLevelGroupedCounters(groupName1(), groupName2(), groupId(), counters);
    }

    public DualLevelGroupedCounters setGroupData(String groupName1, String groupName2, String groupId) {
        return new DualLevelGroupedCounters(groupName1, groupName2, groupId, counters());
    }
}
