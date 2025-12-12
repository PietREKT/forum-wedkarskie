package org.piet.forumbackend.content.reports.dtos.responses;

import lombok.Value;
import org.piet.forumbackend.content.core.entities.Content;
import org.piet.forumbackend.content.reports.entites.ContentReport;
import org.piet.forumbackend.content.reports.entites.enums.ReportReason;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

/**
 * DTO for {@link ContentReport}
 */
@Value
public class ContentReportDto implements Serializable {
    Instant createdAt;
    Long reportCount;
    ReportedContentDto reported;
    Map<ReportReason, Long> reportReasonsCount;

    public static ContentReportDto create(Content content, Long reportCount,Map<ReportReason, Long> count){
        return new ContentReportDto(
            content.getCreatedAt(),
            reportCount,
            ReportedContentDto.create(content),
            count
        );
    }
}