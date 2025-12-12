package org.piet.forumbackend.fishing_spots.opinions.reports.dtos.responses;

import lombok.Value;
import org.piet.forumbackend.fishing_spots.opinions.core.dtos.FishingSpotOpinionDtoMapper;
import org.piet.forumbackend.fishing_spots.opinions.core.dtos.responses.FishingSpotOpinionDto;
import org.piet.forumbackend.fishing_spots.opinions.core.entities.FishingSpotOpinion;
import org.piet.forumbackend.fishing_spots.opinions.reports.dtos.internal.HotReportedOpinionProjectionDto;

import java.io.Serializable;
import java.time.Instant;

@Value
public class HotReportedOpinionsDto implements Serializable {
    FishingSpotOpinionDto opinion;
    Long reportCount;
    Instant opinionCreatedAt;

    public static HotReportedOpinionsDto create(FishingSpotOpinion opinion, HotReportedOpinionProjectionDto projection){
        return new HotReportedOpinionsDto(
                FishingSpotOpinionDtoMapper.toFishingSpotOpinionDto(opinion),
                projection.getReportCount(),
                projection.getCreatedAt()
        );
    }
}
