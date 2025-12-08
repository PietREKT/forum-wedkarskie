package org.piet.forumbackend.reports.services;

import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.piet.forumbackend.reports.dtos.opinions.requests.CreateFishingSpotOpinionReportDto;
import org.piet.forumbackend.reports.dtos.opinions.responses.HotReportedOpinionsDto;
import org.piet.forumbackend.reports.dtos.opinions.responses.OpinionReportDto;
import org.piet.forumbackend.reports.entities.FishingSpotOpinionReport;
import org.springframework.data.domain.Page;

import java.time.temporal.TemporalUnit;

public interface FishingSpotOpinionReportService {
    void dismissReports(Long opinionId);

    Page<OpinionReportDto> getReportsByOpinionId(Long opinionId);

    FishingSpotOpinionReport createReport(CreateFishingSpotOpinionReportDto dto);

    Page<HotReportedOpinionsDto> getRecentlyReportedOpinions(Long timeAmount, TemporalUnit unit, PaginationDto pagination);

    OpinionReportDto getReportSummary();
}
