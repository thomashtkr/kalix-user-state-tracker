package be.htkr.jnj.kalix.demo.entity.twolevel;

import be.htkr.jnj.kalix.demo.entity.user.UserState;

import java.util.Map;

public record TwoLevelGroupedCounters(String groupName1, String groupName2, String groupId, Map<UserState.Status, Integer> counters) {

    public TwoLevelGroupedCounters count(UserState.Status status, int movement) {
        int newValue = counters.merge(status, movement, Integer::sum);
        if(newValue < 0) {
            counters.put(status,0);
        }
        return new TwoLevelGroupedCounters(groupName1(), groupName2(), groupId(), counters);
    }

    public TwoLevelGroupedCounters setGroupData(String groupName1, String groupName2, String groupId) {
        return new TwoLevelGroupedCounters(groupName1, groupName2, groupId, counters());
    }
}
