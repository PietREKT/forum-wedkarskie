package org.piet.forumbackend.fishing_spots.dtos.requests.opinions;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link org.piet.forumbackend.fishing_spots.entities.FishingSpotOpinion}
 */
@Value
public class CreateFishingSpotOpinionDto implements Serializable {
    @Min(1)
    @Max(5)
    Integer rating;
    String comment;
    Long spotId;
}