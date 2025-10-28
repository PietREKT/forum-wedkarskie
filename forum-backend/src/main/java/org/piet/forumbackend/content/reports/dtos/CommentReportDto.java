package org.piet.forumbackend.content.reports.dtos;

import lombok.Value;
import org.piet.forumbackend.content.comments.dtos.CommentDto;
import org.piet.forumbackend.content.reports.ReportReason;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for {@link org.piet.forumbackend.content.reports.entities.CommentReport}
 */
@Value
public class CommentReportDto implements Serializable {
    Long id;
    Instant createdAt;
    ReportReason reason;
    CommentDto reportedComment;
}