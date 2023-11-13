package be.htkr.jnj.kalix.demo.view;

import java.util.List;

public record SingleLevelGroupedViewData(String groupName, String groupId, List<StatusCounter> counters) {
}
