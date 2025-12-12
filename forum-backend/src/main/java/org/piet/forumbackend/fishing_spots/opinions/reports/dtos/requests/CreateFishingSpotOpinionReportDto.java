package org.piet.forumbackend.fishing_spots.opinions.reports.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.piet.forumbackend.content.reports.entites.enums.ReportReason;
import org.piet.forumbackend.fishing_spots.opinions.reports.entities.FishingSpotOpinionReport;

import java.io.Serializable;

/**
 * DTO for {@link FishingSpotOpinionReport}
 */
@Value
public class CreateFishingSpotOpinionReportDto implements Serializable {
    @NotNull
    ReportReason reason;
    @NotNull
    Long opinionId;
}