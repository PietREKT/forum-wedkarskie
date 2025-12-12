package org.piet.forumbackend.fishing_spots.core.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.piet.forumbackend.fishing_spots.core.entities.FishingSpot;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * DTO for {@link FishingSpot}
 */
@Value
public class CreateFishingSpotDto implements Serializable {
    @NotNull
    String name;
    String description;
    FishingSpot.FISHING_SPOT_TYPE type;
    @NotNull
    List<UUID> managerIds;
    @NotNull
    List<Long> fishIds;
    @NotNull
    LocationDto locationDto;
//    MultipartFile statue;
}