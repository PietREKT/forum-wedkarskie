package org.piet.forumbackend.fishing_spots.dtos;

import lombok.Value;
import org.piet.forumbackend.fish.dtos.GetFishDto;
import org.piet.forumbackend.fishing_spots.FishingSpot;
import org.piet.forumbackend.users.dtos.GetUserDto;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link FishingSpot}
 */
@Value
public class CreateFishingSpotDto implements Serializable {
    String name;
    String description;
    FishingSpot.FISHING_SPOT_TYPE type;
    List<GetUserDto> managers;
    List<GetFishDto> fish;
}