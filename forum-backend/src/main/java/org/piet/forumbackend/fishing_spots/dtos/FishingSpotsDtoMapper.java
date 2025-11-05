package org.piet.forumbackend.fishing_spots.dtos;

import org.piet.forumbackend.fishing_spots.entities.Fish;

public class FishingSpotsDtoMapper {
    public static FishDto toFishDto(Fish fish) {
        return new FishDto(
                fish.getName(),
                fish.getAvgLength(),
                fish.getAvgMass(),
                fish.getPhotoUrl(),
                fish.getDescription(),
                fish.isPredatory(),
                fish.getMethods(),
                fish.getWaterType()
        );
    }
}
