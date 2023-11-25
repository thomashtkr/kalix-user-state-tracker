package be.htkr.jnj.kalix.demo.entity.singlelevel;

import be.htkr.jnj.kalix.demo.entity.user.UserState;
import be.htkr.jnj.kalix.demo.view.GroupingName;
import kalix.javasdk.testkit.ValueEntityTestKit;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class StatusPerPeriodTest {

    @Test
    void testInitialState() {
        var testKit = ValueEntityTestKit.of(SingleLevelGroupingEntity::new);
        {
            assertThat(testKit.getState().counters()).isEmpty();
        }
    }


    @Test
    void testStateUpdate(){
        SingleLevelGroupedCounters state = new SingleLevelGroupedCounters("groupName", "groupId", new HashMap<>());
        state.count(UserState.Status.REGISTERED, 1);
        assertThat(state.counters().get(UserState.Status.REGISTERED)).isEqualTo(1);
        assertThat(state.counters().entrySet()).hasSize(1);

        state.count(UserState.Status.EMAIL_VERIFIED, 10);
        assertThat(state.counters().get(UserState.Status.EMAIL_VERIFIED)).isEqualTo(10);
        assertThat(state.counters().get(UserState.Status.REGISTERED)).isEqualTo(1);
        assertThat(state.counters().entrySet()).hasSize(2);


        state.count(UserState.Status.REGISTERED, -1);
        assertThat(state.counters().get(UserState.Status.REGISTERED)).isEqualTo(0);
        assertThat(state.counters().get(UserState.Status.EMAIL_VERIFIED)).isEqualTo(10);
        assertThat(state.counters().entrySet()).hasSize(2);

        state.count(UserState.Status.GDPR_CONFIRMED, -1);
        assertThat(state.counters().get(UserState.Status.REGISTERED)).isEqualTo(0);
        assertThat(state.counters().get(UserState.Status.EMAIL_VERIFIED)).isEqualTo(10);
        assertThat(state.counters().get(UserState.Status.GDPR_CONFIRMED)).isEqualTo(0);
        assertThat(state.counters().entrySet()).hasSize(3);

        state.count(UserState.Status.EMAIL_VERIFIED, -1);
        assertThat(state.counters().get(UserState.Status.REGISTERED)).isEqualTo(0);
        assertThat(state.counters().get(UserState.Status.EMAIL_VERIFIED)).isEqualTo(9);
        assertThat(state.counters().get(UserState.Status.GDPR_CONFIRMED)).isEqualTo(0);
        assertThat(state.counters().entrySet()).hasSize(3);

        state.count(UserState.Status.EMAIL_VERIFIED, -1);
        assertThat(state.counters().get(UserState.Status.REGISTERED)).isEqualTo(0);
        assertThat(state.counters().get(UserState.Status.EMAIL_VERIFIED)).isEqualTo(8);
        assertThat(state.counters().get(UserState.Status.GDPR_CONFIRMED)).isEqualTo(0);
        assertThat(state.counters().entrySet()).hasSize(3);
    }

    @Test
    void testCountStatus() {
        var testKit = ValueEntityTestKit.of(SingleLevelGroupingEntity::new);
        {
            boolean wasUpdated = testKit.call(e -> e.registerMovement(GroupingName.PER_YEAR.value, "2023", new RegisterStatusMovementCommand(UserState.Status.REGISTERED, 1)))
                    .stateWasUpdated();
            assertThat(wasUpdated).isTrue();
            assertThat(testKit.getState().counters().get(UserState.Status.REGISTERED)).isEqualTo(1);
            assertThat(testKit.getState().counters()).containsExactlyEntriesOf(Map.of(UserState.Status.REGISTERED, 1));
            assertThat(testKit.getState().groupName()).isEqualTo(GroupingName.PER_YEAR.value);
            assertThat(testKit.getState().groupId()).isEqualTo("2023");
        }
    }

}
