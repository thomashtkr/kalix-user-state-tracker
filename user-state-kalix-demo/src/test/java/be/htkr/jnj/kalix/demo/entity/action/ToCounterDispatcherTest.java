package be.htkr.jnj.kalix.demo.entity.action;

import be.htkr.jnj.kalix.demo.view.PeriodGroupingName;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

public class ToCounterDispatcherTest {

    @Test
    void testPeriodIds() {
        var in2023 = Instant.parse("2023-01-09T00:00:00.00Z");
        var jan = Instant.parse("2023-01-09T00:00:00.00Z");
        var mar = Instant.parse("2023-03-09T00:00:00.00Z");
        var apr = Instant.parse("2023-04-09T00:00:00.00Z");
        var jun = Instant.parse("2023-06-09T00:00:00.00Z");
        var jul = Instant.parse("2023-07-09T00:00:00.00Z");
        var sep = Instant.parse("2023-09-09T00:00:00.00Z");
        var oct = Instant.parse("2023-10-09T00:00:00.00Z");
        var dec = Instant.parse("2023-12-09T00:00:00.00Z");

        Assertions.assertThat(PeriodGroupingName.timeStampToPeriodId(in2023, PeriodGroupingName.PER_YEAR)).isEqualTo("2023");

        Assertions.assertThat(PeriodGroupingName.timeStampToPeriodId(jan, PeriodGroupingName.PER_MONTH)).isEqualTo("2023M01");
        Assertions.assertThat(PeriodGroupingName.timeStampToPeriodId(mar, PeriodGroupingName.PER_MONTH)).isEqualTo("2023M03");
        Assertions.assertThat(PeriodGroupingName.timeStampToPeriodId(apr, PeriodGroupingName.PER_MONTH)).isEqualTo("2023M04");


        Assertions.assertThat(PeriodGroupingName.timeStampToPeriodId(jan, PeriodGroupingName.PER_QUARTER)).isEqualTo("2023Q1");
        Assertions.assertThat(PeriodGroupingName.timeStampToPeriodId(mar, PeriodGroupingName.PER_QUARTER)).isEqualTo("2023Q1");
        Assertions.assertThat(PeriodGroupingName.timeStampToPeriodId(apr, PeriodGroupingName.PER_QUARTER)).isEqualTo("2023Q2");
        Assertions.assertThat(PeriodGroupingName.timeStampToPeriodId(jun, PeriodGroupingName.PER_QUARTER)).isEqualTo("2023Q2");
        Assertions.assertThat(PeriodGroupingName.timeStampToPeriodId(jul, PeriodGroupingName.PER_QUARTER)).isEqualTo("2023Q3");
        Assertions.assertThat(PeriodGroupingName.timeStampToPeriodId(sep, PeriodGroupingName.PER_QUARTER)).isEqualTo("2023Q3");
        Assertions.assertThat(PeriodGroupingName.timeStampToPeriodId(oct, PeriodGroupingName.PER_QUARTER)).isEqualTo("2023Q4");
        Assertions.assertThat(PeriodGroupingName.timeStampToPeriodId(dec, PeriodGroupingName.PER_QUARTER)).isEqualTo("2023Q4");


    }
}
