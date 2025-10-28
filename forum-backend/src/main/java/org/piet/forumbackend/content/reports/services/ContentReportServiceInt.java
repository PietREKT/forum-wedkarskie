package org.piet.forumbackend.content.reports.services;

import org.piet.forumbackend.content.ContentBase;
import org.piet.forumbackend.content.reports.ReportReason;
import org.piet.forumbackend.content.reports.entities.ContentReport;
import org.piet.forumbackend.content.reports.exceptions.ContentReportNotFoundException;
import org.piet.forumbackend.users.entities.User;

import java.util.List;

public interface ContentReportServiceInt<T extends ContentReport, CB extends ContentBase> {
    public T getReportById(Long id) throws ContentReportNotFoundException;

    public T createReport(ReportReason reportReason, CB reported, User reportedBy);

    public void deleteReport(Long id);

    public List<User> getUsersWhoReportedContent(Long contentId) throws ContentReportNotFoundException;

    public List<T> getReportsByContentId(Long contentId);
}
