package be.htkr.jnj.kalix.demo.entity.action;

import be.htkr.jnj.kalix.demo.view.GroupingName;
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

        Assertions.assertThat(GroupingName.timeStampToPeriodId(in2023, GroupingName.PER_YEAR)).isEqualTo("2023");

        Assertions.assertThat(GroupingName.timeStampToPeriodId(jan, GroupingName.PER_MONTH)).isEqualTo("2023M01");
        Assertions.assertThat(GroupingName.timeStampToPeriodId(mar, GroupingName.PER_MONTH)).isEqualTo("2023M03");
        Assertions.assertThat(GroupingName.timeStampToPeriodId(apr, GroupingName.PER_MONTH)).isEqualTo("2023M04");


        Assertions.assertThat(GroupingName.timeStampToPeriodId(jan, GroupingName.PER_QUARTER)).isEqualTo("2023Q1");
        Assertions.assertThat(GroupingName.timeStampToPeriodId(mar, GroupingName.PER_QUARTER)).isEqualTo("2023Q1");
        Assertions.assertThat(GroupingName.timeStampToPeriodId(apr, GroupingName.PER_QUARTER)).isEqualTo("2023Q2");
        Assertions.assertThat(GroupingName.timeStampToPeriodId(jun, GroupingName.PER_QUARTER)).isEqualTo("2023Q2");
        Assertions.assertThat(GroupingName.timeStampToPeriodId(jul, GroupingName.PER_QUARTER)).isEqualTo("2023Q3");
        Assertions.assertThat(GroupingName.timeStampToPeriodId(sep, GroupingName.PER_QUARTER)).isEqualTo("2023Q3");
        Assertions.assertThat(GroupingName.timeStampToPeriodId(oct, GroupingName.PER_QUARTER)).isEqualTo("2023Q4");
        Assertions.assertThat(GroupingName.timeStampToPeriodId(dec, GroupingName.PER_QUARTER)).isEqualTo("2023Q4");


    }
}
