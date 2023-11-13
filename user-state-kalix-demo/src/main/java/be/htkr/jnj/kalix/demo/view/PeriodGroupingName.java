package be.htkr.jnj.kalix.demo.view;

import be.htkr.jnj.kalix.demo.DemoConfig;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.IsoFields;

public enum PeriodGroupingName {
    PER_YEAR("peryear"),
    PER_MONTH("permonth"),
    PER_QUARTER("perquarter"),

    PER_AGEGROUP("peragegroup");


    public final String value;

    PeriodGroupingName(String value) {
        this.value = value;
    }


    public static String timeStampToPeriodId(Instant timestamp, PeriodGroupingName periodName) {
        return switch (periodName) {
            case PER_YEAR -> String.valueOf(timestamp.atZone(ZoneId.systemDefault()).getYear());
            case PER_MONTH ->
                    String.format("%sM%02d", timeStampToPeriodId(timestamp, PER_YEAR), timestamp.atZone(ZoneId.systemDefault()).getMonthValue());
            case PER_QUARTER -> String.format("%sQ%d", timeStampToPeriodId(timestamp, PER_YEAR), getQuarter(timestamp));
            default -> throw new IllegalArgumentException(periodName + " is not period based. Cannot create a groupId for it");
        };
    }

    private static int getQuarter(Instant timestamp) {
        return LocalDate.ofInstant(timestamp, ZoneId.systemDefault()).get(IsoFields.QUARTER_OF_YEAR);
    }
}
