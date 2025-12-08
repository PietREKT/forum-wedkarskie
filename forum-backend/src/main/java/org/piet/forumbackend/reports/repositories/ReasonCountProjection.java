package org.piet.forumbackend.reports.repositories;

import org.piet.forumbackend.reports.entities.enums.ReportReason;

public interface ReasonCountProjection {
    ReportReason getReason();
    Long getCount();
}
