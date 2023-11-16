package be.htkr.jnj.kalix.demo.view.singlelevel;

import be.htkr.jnj.kalix.demo.view.StatusCounter;

import java.util.List;

public record SingleLevelGroupedViewData(String groupName, String groupId, List<StatusCounter> counters) {
}
