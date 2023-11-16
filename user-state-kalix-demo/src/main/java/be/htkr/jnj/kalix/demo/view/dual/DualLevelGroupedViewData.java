package be.htkr.jnj.kalix.demo.view.dual;

import be.htkr.jnj.kalix.demo.view.StatusCounter;

import java.util.List;

public record DualLevelGroupedViewData(String group1, String group2, String groupId, List<StatusCounter> counters) {
}
