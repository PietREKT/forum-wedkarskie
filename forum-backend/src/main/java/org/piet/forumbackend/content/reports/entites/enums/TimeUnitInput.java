package org.piet.forumbackend.content.reports.entites.enums;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public enum TimeUnitInput {
    HOURS,
    DAYS,
    WEEKS;

    public TemporalUnit map(){
        return switch (this){
            case HOURS -> ChronoUnit.HOURS;
            case DAYS -> ChronoUnit.DAYS;
            case WEEKS -> ChronoUnit.WEEKS;
        };
    }
}
