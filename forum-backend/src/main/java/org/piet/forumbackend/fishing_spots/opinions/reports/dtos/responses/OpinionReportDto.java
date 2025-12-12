package org.piet.forumbackend.fishing_spots.opinions.reports.dtos.responses;

import lombok.Value;
import org.piet.forumbackend.content.reports.entites.enums.ReportReason;
import org.piet.forumbackend.fishing_spots.opinions.core.dtos.responses.FishingSpotOpinionDto;

import java.io.Serializable;
import java.util.Map;

@Value
public class OpinionReportDto implements Serializable {
    FishingSpotOpinionDto opinion;
    Long reportCount;
    Map<ReportReason, Long> reportReasonsCount;
}
