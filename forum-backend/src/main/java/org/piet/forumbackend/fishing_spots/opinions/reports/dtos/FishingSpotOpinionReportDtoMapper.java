package org.piet.forumbackend.fishing_spots.opinions.reports.dtos;

import org.piet.forumbackend.content.reports.entites.enums.ReportReason;
import org.piet.forumbackend.fishing_spots.opinions.core.dtos.FishingSpotOpinionDtoMapper;
import org.piet.forumbackend.fishing_spots.opinions.core.entities.FishingSpotOpinion;
import org.piet.forumbackend.fishing_spots.opinions.reports.dtos.internal.HotReportedOpinionProjectionDto;
import org.piet.forumbackend.fishing_spots.opinions.reports.dtos.responses.HotReportedOpinionsDto;
import org.piet.forumbackend.fishing_spots.opinions.reports.dtos.responses.OpinionReportDto;

import java.util.Map;

public class FishingSpotOpinionReportDtoMapper {
    public static HotReportedOpinionsDto toHotReportedOpinionsDto(FishingSpotOpinion opinion, HotReportedOpinionProjectionDto dto){
        return new HotReportedOpinionsDto(
                FishingSpotOpinionDtoMapper.toFishingSpotOpinionDto(opinion),
                dto.getReportCount(),
                opinion.getCreatedAt()
        );
    }

    public static OpinionReportDto toOpinionReportDto(FishingSpotOpinion opinion, Long reportCount, Map<ReportReason, Long> count){
        return new OpinionReportDto(
                FishingSpotOpinionDtoMapper.toFishingSpotOpinionDto(opinion),
                reportCount,
                count
        );
    }
}
