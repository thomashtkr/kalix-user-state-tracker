package be.htkr.jnj.kalix.demo.entity.user;

import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class AgeGroupTriggerCalculator {
    public LocalDate getNextBirthDayTriggerDate(LocalDate today, LocalDate born) {
        var next = born.withYear(today.getYear()).withMonth(today.getMonthValue());
        if(next.isBefore(today)){
            return next.plusMonths(1);
        } else {
            return next;
        }
    }

}
