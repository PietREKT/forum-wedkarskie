package org.piet.forumbackend.reports.dtos.content.internal;

import lombok.Value;

import java.io.Serializable;
import java.time.Instant;

@Value
public class HotReportedContentProjectionDto implements Serializable {
    Long contentId;
    Long reportCount;
    Instant createdAt;
    Double score;
}
