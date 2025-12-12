package org.piet.forumbackend.fishing_spots.opinions.reports.dtos.internal;

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
