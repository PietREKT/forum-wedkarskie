package org.piet.forumbackend.reports.dtos.opinions;

import org.piet.forumbackend.fishing_spots.dtos.FishingSpotOpinionDtoMapper;
import org.piet.forumbackend.fishing_spots.entities.FishingSpotOpinion;
import org.piet.forumbackend.reports.dtos.opinions.internal.HotReportedOpinionProjectionDto;
import org.piet.forumbackend.reports.dtos.opinions.responses.HotReportedOpinionsDto;
import org.piet.forumbackend.reports.dtos.opinions.responses.OpinionReportDto;
import org.piet.forumbackend.reports.entities.enums.ReportReason;

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
