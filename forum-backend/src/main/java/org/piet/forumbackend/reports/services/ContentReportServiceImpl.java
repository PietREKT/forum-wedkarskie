package org.piet.forumbackend.reports.services;

import org.piet.forumbackend.content.entities.Content;
import org.piet.forumbackend.content.repositories.ContentRepository;
import org.piet.forumbackend.globals.exceptions.BadRequestException;
import org.piet.forumbackend.reports.dtos.content.internal.HotReportedContentProjectionDto;
import org.piet.forumbackend.reports.dtos.content.responses.ContentReportDto;
import org.piet.forumbackend.reports.dtos.content.responses.HotReportedContentDto;
import org.piet.forumbackend.reports.entities.ContentReport;
import org.piet.forumbackend.reports.entities.enums.ReportReason;
import org.piet.forumbackend.reports.repositories.ContentReportRepository;
import org.piet.forumbackend.reports.repositories.ReasonCountProjection;
import org.piet.forumbackend.users.core.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ContentReportServiceImpl implements ContentReportService {
    private final ContentReportRepository contentReportRepository;
    private final ContentRepository contentRepository;

    public ContentReportServiceImpl(ContentReportRepository contentReportRepository, ContentRepository contentRepository) {
        this.contentReportRepository = contentReportRepository;
        this.contentRepository = contentRepository;
    }

    @Override
    public List<ContentReport> getReportsByContentId(Content content) {
        return contentReportRepository.findByReported(content);
    }

    @Override
    public Page<ContentReport> getReportsByContentId(Content content, Integer pageNo, Integer pageSize) {
        return contentReportRepository.findByReported(content, PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @Override
    public ContentReport create(Content reportedContent, User reportedBy, ReportReason reason) throws BadRequestException {
        if (reportedContent == null)
            throw new BadRequestException("Invalid content ID provided");

        ContentReport contentReport = new ContentReport();
        contentReport.setReportedBy(reportedBy);
        contentReport.setReason(reason);
        contentReport.setReported(reportedContent);
        return contentReportRepository.save(contentReport);
    }

    @Override
    public void dismissReportsByReason(Content content, ReportReason reason) {
        contentReportRepository.deleteByReportedAndReason(content, reason);
    }

    @Override
    public void dismissReports(Long contentId) {
        contentReportRepository.deleteContentReportsByReported_Id(contentId);
    }

    @Override
    public Long getReportsCountForContent(Content content) {
        return contentReportRepository.countByReported(content);
    }

    @Override
    public List<HotReportedContentDto> getRecentlyReportedContent(Long amount, ChronoUnit unit, Integer pageNo, Integer pageSize) {
        Instant now = Instant.now();

        Instant since = Instant.now().minus(amount, unit);
        List<ContentReportRepository.HotReportProjection> projections = contentReportRepository.getHotReports(since);
        var aggregated = projections
                .stream()
                .map(proj -> {
                    long ageMinutes = Duration.between(proj.getCreatedAt(), now).toMinutes();
                    double age = Math.max(ageMinutes, 1);
                    double score = proj.getReportCount() / age;
                    return new HotReportedContentProjectionDto(
                            proj.getContentId(),
                            proj.getReportCount(),
                            proj.getCreatedAt(),
                            score
                    );
                })
                .sorted(Comparator.comparingDouble(HotReportedContentProjectionDto::getScore).reversed())
                .skip((long) pageNo * pageSize)
                .limit(pageSize)
                .toList();

        Map<Long, Content> contentMap = contentRepository.findAllById(
                        aggregated.stream().map(HotReportedContentProjectionDto::getContentId).toList()
                )
                .stream()
                .collect(Collectors.toMap(Content::getId, c -> c));

        return aggregated.stream().map(proj ->
                HotReportedContentDto.create(contentMap.get(proj.getContentId()), proj)
        ).toList();
    }

    @Override
    public ContentReportDto getReportSummary(Content content) {
        Long totalCount = getReportsCountForContent(content);

        var map = contentReportRepository.countByReason(content)
                .stream()
                .collect(Collectors.toMap(
                        ReasonCountProjection::getReason,
                        ReasonCountProjection::getCount
                ));
        return ContentReportDto.create(content, totalCount, map);
    }
}
