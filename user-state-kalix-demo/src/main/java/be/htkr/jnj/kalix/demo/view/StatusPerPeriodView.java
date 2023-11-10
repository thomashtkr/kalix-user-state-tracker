package be.htkr.jnj.kalix.demo.view;

import be.htkr.jnj.kalix.demo.entity.statusperperiod.StatusPerPeriod;
import be.htkr.jnj.kalix.demo.entity.statusperperiod.StatusPerPeriodEntity;
import be.htkr.jnj.kalix.demo.entity.user.UserState;
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

@ViewId("view_status_per_period")
@Table("status_per_period")
public class StatusPerPeriodView extends View<StatusPerPeriodViewData> {

    @Subscribe.ValueEntity(StatusPerPeriodEntity.class)
    public UpdateEffect<StatusPerPeriodViewData> onChange(StatusPerPeriod perPeriod) {
        return effects()
                .updateState(new StatusPerPeriodViewData(perPeriod.periodName(), perPeriod.periodId(), fromCounterMap(perPeriod.counters()) ));
    }

    private List<StatusCounter> fromCounterMap(Map<UserState.Status, Integer> counters) {
        return counters.entrySet().stream().map(e -> new StatusCounter(e.getKey().name(), e.getValue())).collect(Collectors.toList());
    }



    @GetMapping("/view/counters/{periodName}/{periodId}")
    @Query("SELECT * FROM status_per_period WHERE periodName = :periodName and periodId = :periodId" )
    public StatusPerPeriodViewData getStatusPerPeriod(@PathVariable("periodName") String periodName, @PathVariable("periodId") String periodId) {
        return null;
    }

}
