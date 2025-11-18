package org.piet.forumbackend.content.reports.dtos;

import lombok.Value;
import org.piet.forumbackend.content.reports.entities.enums.ReportReason;

import java.io.Serializable;

/**
 * DTO for {@link org.piet.forumbackend.content.reports.entities.ContentReport}
 */
@Value
public class CreateContentReportDto implements Serializable {
    ReportReason reason;
    Long contentId;
}