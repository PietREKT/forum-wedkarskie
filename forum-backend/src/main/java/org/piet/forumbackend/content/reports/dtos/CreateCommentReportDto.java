package org.piet.forumbackend.content.reports.dtos;

import lombok.Value;
import org.piet.forumbackend.content.reports.ReportReason;

import java.io.Serializable;

/**
 * DTO for {@link org.piet.forumbackend.content.reports.entities.CommentReport}
 */
@Value
public class CreateCommentReportDto implements Serializable {
    ReportReason reason;
    Long commentId;
}