package org.piet.forumbackend.fish.dtos;

import org.piet.forumbackend.fish.entities.Fish;

public class FishDtoMapper {
    public static FishDto toFishDto(Fish fish) {
        return FishDto.create(fish);
    }

    public static FishListDto toFishListDto(Fish fish){
        return FishListDto.create(fish);
    }
}
