package org.piet.forumbackend.fish.dtos;

import org.piet.forumbackend.fish.entities.Fish;

public class FishDtoMapper {
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
