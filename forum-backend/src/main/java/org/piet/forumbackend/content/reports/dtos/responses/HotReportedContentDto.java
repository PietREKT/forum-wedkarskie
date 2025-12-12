package org.piet.forumbackend.content.reports.dtos.responses;

import lombok.Value;
import org.piet.forumbackend.content.core.entities.Content;
import org.piet.forumbackend.content.reports.dtos.internal.HotReportedContentProjectionDto;

import java.io.Serializable;
import java.time.Instant;

@Value
public class HotReportedContentDto implements Serializable {
    ReportedContentDto content;
    Long reportCount;
    Instant createdAt;

    public static HotReportedContentDto create(Content content, HotReportedContentProjectionDto projectionDto){
        if (!content.getId().equals(projectionDto.getContentId()))
            throw new IllegalArgumentException("Content's ids don't match!");

        return new HotReportedContentDto(
                ReportedContentDto.create(content),
                projectionDto.getReportCount(),
                projectionDto.getCreatedAt()
        );
    }
}
