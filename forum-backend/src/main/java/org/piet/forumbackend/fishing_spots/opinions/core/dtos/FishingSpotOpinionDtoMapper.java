package org.piet.forumbackend.fishing_spots.opinions.core.dtos;

import org.piet.forumbackend.fishing_spots.opinions.core.dtos.responses.FishingSpotOpinionDto;
import org.piet.forumbackend.fishing_spots.opinions.core.entities.FishingSpotOpinion;
import org.piet.forumbackend.users.core.dtos.UsersDtoMapper;

public class FishingSpotOpinionDtoMapper {
    public static FishingSpotOpinionDto toFishingSpotOpinionDto(FishingSpotOpinion opinion){
        return new FishingSpotOpinionDto(
                opinion.getId(),
                opinion.getRating(),
                opinion.getComment(),
                UsersDtoMapper.toListUserDto(opinion.getAuthor()),
                opinion.getCreatedAt()
        );
    }
}
