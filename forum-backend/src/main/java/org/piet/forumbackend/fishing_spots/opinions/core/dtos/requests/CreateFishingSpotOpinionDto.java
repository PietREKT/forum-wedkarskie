package org.piet.forumbackend.fishing_spots.opinions.core.dtos.requests;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Value;
import org.piet.forumbackend.fishing_spots.opinions.core.entities.FishingSpotOpinion;

import java.io.Serializable;

/**
 * DTO for {@link FishingSpotOpinion}
 */
@Value
public class CreateFishingSpotOpinionDto implements Serializable {
    @Min(1)
    @Max(5)
    Integer rating;
    String comment;
    Long spotId;
}