package org.piet.forumbackend.fishing_spots.opinions.reports.services;

import org.piet.forumbackend.fishing_spots.opinions.reports.dtos.requests.CreateFishingSpotOpinionReportDto;
import org.piet.forumbackend.fishing_spots.opinions.reports.dtos.responses.HotReportedOpinionsDto;
import org.piet.forumbackend.fishing_spots.opinions.reports.dtos.responses.OpinionReportDto;
import org.piet.forumbackend.fishing_spots.opinions.reports.entities.FishingSpotOpinionReport;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.springframework.data.domain.Page;

import java.time.temporal.ChronoUnit;

public interface FishingSpotOpinionReportService {
    void dismissReports(Long opinionId);

    Page<OpinionReportDto> getReportsByOpinionId(Long opinionId);

    FishingSpotOpinionReport createReport(CreateFishingSpotOpinionReportDto dto) throws NotFoundException;

    Page<HotReportedOpinionsDto> getRecentlyReportedOpinions(Long timeAmount, ChronoUnit unit, PaginationDto pagination);

    OpinionReportDto getReportSummary(Long opinionId) throws NotFoundException;
}
