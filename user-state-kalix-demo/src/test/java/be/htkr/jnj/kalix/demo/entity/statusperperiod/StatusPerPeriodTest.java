package be.htkr.jnj.kalix.demo.entity.statusperperiod;

import kalix.javasdk.testkit.ValueEntityTestKit;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StatusPerPeriodTest {

    @Test
    void testInitialState() {
        var testKit = ValueEntityTestKit.of(StatusPerPeriodEntity::new);
        {
            assertThat(testKit.getState().counters()).isEmpty();
        }
    }

    /*
    @Test
    void testStateUpdate(){
        StatusPerPeriod state = new StatusPerPeriod("periodName", "periodId", new HashMap<>());
        state.count(UserState.Status.REGISTERED.name(), 1);
        assertThat(state.counters().get(UserState.Status.REGISTERED.name())).isEqualTo(1);
        assertThat(state.counters().entrySet()).hasSize(1);

        state.count(UserState.Status.EMAIL_VERIFIED.name(), 10);
        assertThat(state.counters().get(UserState.Status.EMAIL_VERIFIED.name())).isEqualTo(10);
        assertThat(state.counters().get(UserState.Status.REGISTERED.name())).isEqualTo(1);
        assertThat(state.counters().entrySet()).hasSize(2);


        state.count(UserState.Status.REGISTERED.name(), -1);
        assertThat(state.counters().get(UserState.Status.REGISTERED.name())).isEqualTo(0);
        assertThat(state.counters().get(UserState.Status.EMAIL_VERIFIED.name())).isEqualTo(10);
        assertThat(state.counters().entrySet()).hasSize(2);
    }

     */

    /*
    @Test
    void testCountStatus() {
        var testKit = ValueEntityTestKit.of(StatusPerPeriodEntity::new);
        {
            boolean wasUpdated = testKit.call(e -> e.registerMovement(DemoConfig.PeriodGroupingNames.PER_YEAR, "2023", new RegisterStatusMovementCommand(UserState.Status.REGISTERED, 1)))
                    .stateWasUpdated();
            assertThat(wasUpdated).isTrue();
            assertThat(testKit.getState().counters().get(UserState.Status.REGISTERED.name())).isEqualTo(1);
            assertThat(testKit.getState().counters()).containsExactlyEntriesOf(Map.of(UserState.Status.REGISTERED.name(), 1));
            assertThat(testKit.getState().periodName()).isEqualTo(DemoConfig.PeriodGroupingNames.PER_YEAR);
            assertThat(testKit.getState().periodId()).isEqualTo("2023");
        }
    }

     */
}
