package org.piet.forumbackend.reports.dtos.content.requests;

import lombok.Value;
import org.piet.forumbackend.reports.entities.ContentReport;
import org.piet.forumbackend.reports.entities.enums.ReportReason;

import java.io.Serializable;

/**
 * DTO for {@link ContentReport}
 */
@Value
public class CreateContentReportDto implements Serializable {
    ReportReason reason;
    Long contentId;
}