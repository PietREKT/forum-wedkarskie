package org.piet.forumbackend.reports.services;

import org.piet.forumbackend.fishing_spots.entities.FishingSpotOpinion;
import org.piet.forumbackend.fishing_spots.repositories.FishingSpotOpinionRepository;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.piet.forumbackend.reports.dtos.opinions.internal.HotReportedOpinionProjectionDto;
import org.piet.forumbackend.reports.dtos.opinions.requests.CreateFishingSpotOpinionReportDto;
import org.piet.forumbackend.reports.dtos.opinions.responses.HotReportedOpinionsDto;
import org.piet.forumbackend.reports.dtos.opinions.responses.OpinionReportDto;
import org.piet.forumbackend.reports.entities.FishingSpotOpinionReport;
import org.piet.forumbackend.reports.repositories.FishingSpotOpinionReportRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class FishingSpotOpinionReportServiceImpl implements FishingSpotOpinionReportService {
    private final FishingSpotOpinionReportRepository fishingSpotOpinionReportRepository;
    private final FishingSpotOpinionRepository fishingSpotOpinionRepository;

    public FishingSpotOpinionReportServiceImpl(FishingSpotOpinionReportRepository fishingSpotOpinionReportRepository, FishingSpotOpinionRepository fishingSpotOpinionRepository) {
        this.fishingSpotOpinionReportRepository = fishingSpotOpinionReportRepository;
        this.fishingSpotOpinionRepository = fishingSpotOpinionRepository;
    }

    @Override
    public void dismissReports(Long opinionId) {
        fishingSpotOpinionReportRepository.deleteAllByOpinion_Id(opinionId);
    }

    @Override
    public Page<OpinionReportDto> getReportsByOpinionId(Long opinionId) {
        return null;
    }

    @Override
    public FishingSpotOpinionReport createReport(CreateFishingSpotOpinionReportDto dto) {
        return null;
    }

    @Override
    public Page<HotReportedOpinionsDto> getRecentlyReportedOpinions(Long timeAmount, TemporalUnit unit, PaginationDto pagination) {
        Instant now = Instant.now();
        Instant since = now.minus(timeAmount, unit);
        var scored = fishingSpotOpinionReportRepository
                .findAllHotOpinions(since)
                .stream()
                .map(proj -> {
                    long ageMinutes = Duration.between(proj.getCreatedAt(), now).toMinutes();
                    double age = Math.max(ageMinutes, 1);
                    double score = proj.getReportCount() / age;

                    return new HotReportedOpinionProjectionDto(
                            proj.getOpinionId(),
                            proj.getReportCount(),
                            proj.getCreatedAt(),
                            score
                    );
                })
                .sorted(
                        Comparator.comparingDouble(HotReportedOpinionProjectionDto::getScore)
                                .reversed()
                                .thenComparingLong(HotReportedOpinionProjectionDto::getReportCount)
                )
                .toList();
        Pageable pageable = pagination.toPageable();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pagination.getSize(), scored.size());

        List<HotReportedOpinionProjectionDto> pageContent = start >= scored.size() ? List.of() : scored.subList(start, end);

        Page<HotReportedOpinionProjectionDto> projectionDtoPage =
                new PageImpl<>(pageContent, pageable, scored.size());

        Map<Long, FishingSpotOpinion> opinionMap = fishingSpotOpinionRepository
                .findAllById(
                        pageContent.stream().map(HotReportedOpinionProjectionDto::getOpinionId).toList()
                )
                .stream()
                .collect(Collectors.toMap(FishingSpotOpinion::getId, Function.identity()));

        return projectionDtoPage.map(
                proj -> HotReportedOpinionsDto.create(opinionMap.get(proj.getOpinionId()), proj)
        );
    }

    @Override
    public OpinionReportDto getReportSummary() {
        return null;
    }
}
