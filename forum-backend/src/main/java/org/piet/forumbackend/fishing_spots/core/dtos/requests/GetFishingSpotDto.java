package org.piet.forumbackend.fishing_spots.core.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.piet.forumbackend.fishing_spots.core.entities.FishingSpot;

import java.io.Serializable;

/**
 * DTO for {@link FishingSpot}
 */
@Value
public class GetFishingSpotDto implements Serializable {
    @NotNull
    Long id;
}