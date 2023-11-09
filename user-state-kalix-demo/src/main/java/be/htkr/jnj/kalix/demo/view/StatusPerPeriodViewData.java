package be.htkr.jnj.kalix.demo.view;

import java.util.List;

public record StatusPerPeriodViewData(String periodName, String periodId, List<StatusCounter> counters) {

    public StatusPerPeriodViewData count(String status, int movement) {
        //counters.merge(status, movement, Integer::sum);
        return new StatusPerPeriodViewData(periodName(), periodId(), counters);
    }

    public StatusPerPeriodViewData setGroupData(String periodName, String periodId) {
        return new StatusPerPeriodViewData(periodName, periodId, counters());
    }
}
