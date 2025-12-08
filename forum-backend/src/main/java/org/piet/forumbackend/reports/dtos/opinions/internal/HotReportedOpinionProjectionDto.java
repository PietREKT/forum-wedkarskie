package org.piet.forumbackend.reports.dtos.opinions.internal;

import lombok.Value;

import java.io.Serializable;
import java.time.Instant;

@Value
public class HotReportedOpinionProjectionDto implements Serializable {
    Long opinionId;
    Long reportCount;
    Instant createdAt;
    Double score;
}
