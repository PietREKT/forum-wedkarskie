package org.piet.forumbackend.fishing_spots.opinions.reports.services;

import org.apache.commons.lang3.NotImplementedException;
import org.piet.forumbackend.content.reports.entites.enums.ReportReason;
import org.piet.forumbackend.content.reports.repositories.ReasonCountProjection;
import org.piet.forumbackend.fishing_spots.opinions.core.entities.FishingSpotOpinion;
import org.piet.forumbackend.fishing_spots.opinions.core.repositories.FishingSpotOpinionRepository;
import org.piet.forumbackend.fishing_spots.opinions.core.services.FishingSpotOpinionService;
import org.piet.forumbackend.fishing_spots.opinions.reports.dtos.FishingSpotOpinionReportDtoMapper;
import org.piet.forumbackend.fishing_spots.opinions.reports.dtos.internal.HotReportedOpinionProjectionDto;
import org.piet.forumbackend.fishing_spots.opinions.reports.dtos.requests.CreateFishingSpotOpinionReportDto;
import org.piet.forumbackend.fishing_spots.opinions.reports.dtos.responses.HotReportedOpinionsDto;
import org.piet.forumbackend.fishing_spots.opinions.reports.dtos.responses.OpinionReportDto;
import org.piet.forumbackend.fishing_spots.opinions.reports.entities.FishingSpotOpinionReport;
import org.piet.forumbackend.fishing_spots.opinions.reports.repositories.FishingSpotOpinionReportRepository;
import org.piet.forumbackend.globals.exceptions.BadRequestException;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class FishingSpotOpinionReportServiceImpl implements FishingSpotOpinionReportService {
    private final FishingSpotOpinionReportRepository fishingSpotOpinionReportRepository;
    private final FishingSpotOpinionRepository fishingSpotOpinionRepository;
    private final FishingSpotOpinionService fishingSpotOpinionService;

    public FishingSpotOpinionReportServiceImpl(FishingSpotOpinionReportRepository fishingSpotOpinionReportRepository, FishingSpotOpinionRepository fishingSpotOpinionRepository, FishingSpotOpinionService fishingSpotOpinionService) {
        this.fishingSpotOpinionReportRepository = fishingSpotOpinionReportRepository;
        this.fishingSpotOpinionRepository = fishingSpotOpinionRepository;
        this.fishingSpotOpinionService = fishingSpotOpinionService;
    }

    @Override
    @Transactional
    public void dismissReports(Long opinionId) {
        fishingSpotOpinionReportRepository.deleteAllByOpinion_Id(opinionId);
    }

    @Override
    public Page<OpinionReportDto> getReportsByOpinionId(Long opinionId) {
        throw new NotImplementedException("This feature was not implemented yet!");
    }

    @Override
    @Transactional
    public FishingSpotOpinionReport createReport(CreateFishingSpotOpinionReportDto dto) throws NotFoundException {
        FishingSpotOpinion opinion = fishingSpotOpinionService.getById(dto.getOpinionId());
        FishingSpotOpinionReport report = new FishingSpotOpinionReport();
        report.setOpinion(opinion);
        report.setReason(dto.getReason());
        report.setCreatedAt(Instant.now());
        return fishingSpotOpinionReportRepository.save(report);
    }

    @Override
    public Page<HotReportedOpinionsDto> getRecentlyReportedOpinions(Long timeAmount, ChronoUnit unit, PaginationDto pagination) {
        Instant now = Instant.now();

        Duration duration = switch (unit){
            case MINUTES, HOURS, DAYS, WEEKS -> unit.getDuration().multipliedBy(timeAmount);
            default -> throw new BadRequestException("Unsupported unit: " + unit);
        };

        if (duration.compareTo(ChronoUnit.MONTHS.getDuration()) > 0){
            throw new BadRequestException("You can't query reports older than 1 month");
        }

        Instant since = now.minus(duration);
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
    public OpinionReportDto getReportSummary(Long opinionId) throws NotFoundException {
        long totalCount = fishingSpotOpinionReportRepository.countByOpinion_Id(opinionId);

        Map<ReportReason, Long> reportReasonCount = fishingSpotOpinionReportRepository
                .countByReason(opinionId)
                .stream()
                .collect(Collectors.toMap(ReasonCountProjection::getReason, ReasonCountProjection::getCount));

        FishingSpotOpinion opinion = fishingSpotOpinionService.getById(opinionId);

        return FishingSpotOpinionReportDtoMapper.toOpinionReportDto(opinion, totalCount, reportReasonCount);
    }
}
