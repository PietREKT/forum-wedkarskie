package org.piet.forumbackend.content.reports.services;

import org.piet.forumbackend.content.entities.Content;
import org.piet.forumbackend.content.reports.dtos.ContentReportDto;
import org.piet.forumbackend.content.reports.dtos.HotReportedContentDto;
import org.piet.forumbackend.content.reports.entities.ContentReport;
import org.piet.forumbackend.content.reports.entities.enums.ReportReason;
import org.piet.forumbackend.users.core.entities.User;
import org.springframework.data.domain.Page;

import java.time.temporal.TemporalUnit;
import java.util.List;

public interface ContentReportService {
    public List<ContentReport> getReportsByContentId(Content content);

    public Page<ContentReport> getReportsByContentId(Content content, Integer pageNo, Integer pageSize);

    public ContentReport create(Content reportedContent, User reportedBy, ReportReason reason);

    public void dismissReportsByReason(Content content, ReportReason reason);

    public Long getReportsCountForContent(Content content);

    public List<HotReportedContentDto> getRecentlyReportedContent(Long amount, TemporalUnit unit, Integer pageNo, Integer pageSize);

    public ContentReportDto getReportSummary(Content content);
}
