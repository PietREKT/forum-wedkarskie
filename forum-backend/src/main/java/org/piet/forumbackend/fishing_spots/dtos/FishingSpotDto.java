package org.piet.forumbackend.fishing_spots.dtos;

import lombok.Value;
import org.piet.forumbackend.fish.dtos.FishDto;
import org.piet.forumbackend.fishing_spots.entities.FishingSpot;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link FishingSpot}
 */
@Value
public class FishingSpotDto implements Serializable {
    Long id;
    String name;
    String description;
    double locationX, locationY;
    FishingSpot.FISHING_SPOT_TYPE type;
    String statuteUrl;
    List<FishDto> fish;

    public static FishingSpotDto create(FishingSpot fishingSpot){
        return new FishingSpotDto(
                fishingSpot.getId(),
                fishingSpot.getName(),
                fishingSpot.getDescription(),
                fishingSpot.getLocation().getX(),
                fishingSpot.getLocation().getY(),
                fishingSpot.getType(),
                fishingSpot.getStatuteUrl(),
                fishingSpot.getFish().stream().map(FishDto::create).toList()
        );
    }
}