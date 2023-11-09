package be.htkr.jnj.kalix.demo.entity.statusperperiod;

import be.htkr.jnj.kalix.demo.entity.user.UserState;
import be.htkr.jnj.kalix.demo.view.StatusCounter;

import java.util.List;
import java.util.Map;

public record StatusPerPeriod(String periodName, String periodId, Map<UserState.Status, Integer> counters) {

    public StatusPerPeriod count(UserState.Status status, int movement) {
        counters.merge(status, movement, Integer::sum);
        return new StatusPerPeriod(periodName(), periodId(), counters);
    }

    public StatusPerPeriod setGroupData(String periodName, String periodId) {
        return new StatusPerPeriod(periodName, periodId, counters());
    }
}
