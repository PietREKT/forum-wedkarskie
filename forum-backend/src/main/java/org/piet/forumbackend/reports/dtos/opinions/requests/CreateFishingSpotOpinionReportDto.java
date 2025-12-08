package org.piet.forumbackend.reports.dtos.opinions.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.piet.forumbackend.reports.entities.enums.ReportReason;

import java.io.Serializable;

/**
 * DTO for {@link org.piet.forumbackend.reports.entities.FishingSpotOpinionReport}
 */
@Value
public class CreateFishingSpotOpinionReportDto implements Serializable {
    @NotNull
    ReportReason reason;
    @NotNull
    Long opinionId;
}