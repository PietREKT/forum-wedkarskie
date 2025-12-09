package org.piet.forumbackend.fishing_spots.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link org.piet.forumbackend.fishing_spots.entities.FishingSpot}
 */
@Value
public class GetFishingSpotDto implements Serializable {
    @NotNull
    Long id;
}