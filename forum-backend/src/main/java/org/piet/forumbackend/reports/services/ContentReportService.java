package org.piet.forumbackend.reports.services;

import org.piet.forumbackend.content.entities.Content;
import org.piet.forumbackend.globals.exceptions.BadRequestException;
import org.piet.forumbackend.globals.pagination.PageDto;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.piet.forumbackend.reports.dtos.content.responses.ContentReportDto;
import org.piet.forumbackend.reports.dtos.content.responses.HotReportedContentDto;
import org.piet.forumbackend.reports.entities.ContentReport;
import org.piet.forumbackend.reports.entities.enums.ReportReason;
import org.piet.forumbackend.users.core.entities.User;
import org.springframework.data.domain.Page;

import java.time.temporal.ChronoUnit;

public interface ContentReportService {
    public PageDto<ContentReport> getReportsByContentId(Content content, PaginationDto pagination);

    public Page<ContentReport> getReportsByContentId(Content content, Integer pageNo, Integer pageSize);

    public ContentReport create(Content reportedContent, User reportedBy, ReportReason reason) throws BadRequestException;

    public void dismissReportsByReason(Content content, ReportReason reason);

    public void dismissReports(Long contentId);

    public Long getReportsCountForContent(Content content);

    public PageDto<HotReportedContentDto> getRecentlyReportedContent(Long amount, ChronoUnit unit, PaginationDto pagination) throws BadRequestException;

    public ContentReportDto getReportSummary(Content content);
}
