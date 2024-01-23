package be.htkr.jnj.kalix.demo.entity;

import be.htkr.jnj.kalix.demo.entity.user.AgeGroupTriggerCalculator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class AgeGroupTriggerTest {

    private AgeGroupTriggerCalculator ageGroupTriggerCalculator = new AgeGroupTriggerCalculator();


    @Test
    void testAgeGroupTrigger_sameMonth() {
        LocalDate today = LocalDate.of(2023, 10, 10);
        LocalDate birthDate = LocalDate.of(2000, 10, 20);

        LocalDate nextTrigger = ageGroupTriggerCalculator.getNextBirthDayTriggerDate(today, birthDate);
        assertThat(nextTrigger).hasDayOfMonth(20);
        assertThat(nextTrigger).hasMonthValue(10);
        assertThat(nextTrigger).hasYear(2023);
    }

    @Test
    void testAgeGroupTrigger_FutureMonth() {
        LocalDate today = LocalDate.of(2023, 1, 10);
        LocalDate birthDate = LocalDate.of(2000, 10, 5);

        LocalDate nextTrigger = ageGroupTriggerCalculator.getNextBirthDayTriggerDate(today, birthDate);
        assertThat(nextTrigger).hasDayOfMonth(5);
        assertThat(nextTrigger).hasMonthValue(2);
        assertThat(nextTrigger).hasYear(2023);
    }
}
