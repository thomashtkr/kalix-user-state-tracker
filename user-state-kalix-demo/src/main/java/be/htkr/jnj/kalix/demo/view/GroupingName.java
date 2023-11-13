package be.htkr.jnj.kalix.demo.view;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.IsoFields;

public enum GroupingName {
    PER_YEAR("peryear"),
    PER_MONTH("permonth"),
    PER_QUARTER("perquarter"),

    PER_AGEGROUP("peragegroup");


    public final String value;

    GroupingName(String value) {
        this.value = value;
    }


    public static String timeStampToPeriodId(Instant timestamp, GroupingName periodName) {
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
