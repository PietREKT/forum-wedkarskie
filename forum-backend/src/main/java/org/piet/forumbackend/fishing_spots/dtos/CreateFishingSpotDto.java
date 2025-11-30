package org.piet.forumbackend.fishing_spots.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.piet.forumbackend.fish.dtos.GetFishDto;
import org.piet.forumbackend.fishing_spots.entities.FishingSpot;
import org.piet.forumbackend.users.core.dtos.GetUserDto;

import java.io.Serializable;
import java.util.List;

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
    List<GetUserDto> managers;
    @NotNull
    List<GetFishDto> fish;
    @NotNull
    LocationDto locationDto;
//    MultipartFile statue;
}