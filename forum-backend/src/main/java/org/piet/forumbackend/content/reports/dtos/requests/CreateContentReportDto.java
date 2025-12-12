package org.piet.forumbackend.content.reports.dtos.requests;

import lombok.Value;
import org.piet.forumbackend.content.reports.entites.ContentReport;
import org.piet.forumbackend.content.reports.entites.enums.ReportReason;

import java.io.Serializable;

/**
 * DTO for {@link ContentReport}
 */
@Value
public class CreateContentReportDto implements Serializable {
    ReportReason reason;
    Long contentId;
}