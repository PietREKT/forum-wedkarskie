package org.piet.forumbackend.reports.dtos.opinions.responses;

import lombok.Value;
import org.piet.forumbackend.fishing_spots.dtos.responses.FishingSpotOpinionDto;
import org.piet.forumbackend.reports.entities.enums.ReportReason;

import java.io.Serializable;
import java.util.Map;

@Value
public class OpinionReportDto implements Serializable {
    FishingSpotOpinionDto opinion;
    Long reportCount;
    Map<ReportReason, Long> reportReasonsCount;
}
