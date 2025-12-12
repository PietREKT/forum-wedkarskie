package org.piet.forumbackend.content.reports.repositories;

import org.piet.forumbackend.content.reports.entites.enums.ReportReason;

public interface ReasonCountProjection {
    ReportReason getReason();
    Long getCount();
}
