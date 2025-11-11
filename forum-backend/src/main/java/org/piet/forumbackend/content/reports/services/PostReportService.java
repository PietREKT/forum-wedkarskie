package org.piet.forumbackend.content.reports.services;

import org.piet.forumbackend.content.posts.entities.Post;
import org.piet.forumbackend.content.reports.ReportReason;
import org.piet.forumbackend.content.reports.entities.ContentReport;
import org.piet.forumbackend.content.reports.entities.PostReport;
import org.piet.forumbackend.content.reports.repositories.PostReportRepository;
import org.piet.forumbackend.exceptions.NotFoundException;
import org.piet.forumbackend.users.entities.User;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostReportService implements ContentReportServiceInt<PostReport, Post>{
    private final PostReportRepository postReportRepository;
    private final MessageSource messageSource;

    public PostReportService(PostReportRepository postReportRepository, MessageSource messageSource) {
        this.postReportRepository = postReportRepository;
        this.messageSource = messageSource;
    }

    @Override
    public PostReport getReportById(Long id) throws NotFoundException {
        return postReportRepository.findById(id).orElseThrow(() -> new NotFoundException(
                messageSource.getMessage("errors.posts.reports.not_found",
                new Object[]{id},
                LocaleContextHolder.getLocale()
        )));
    }

    @Override
    public PostReport createReport(ReportReason reportReason, Post reported, User reportedBy) {
        PostReport postReport = new PostReport();
        postReport.setReason(reportReason);
        postReport.setReportedPost(reported);
        postReport.setReportedBy(reportedBy);
        return postReportRepository.save(postReport);
    }

    @Override
    public void deleteReport(Long id) {
        postReportRepository.deleteById(id);
    }

    @Override
    public List<User> getUsersWhoReportedContent(Long contentId) throws NotFoundException {
        return postReportRepository.findByReportedPost_Id(contentId).stream().map(ContentReport::getReportedBy).toList();
    }

    @Override
    public List<PostReport> getReportsByContentId(Long contentId) {
        return postReportRepository.findByReportedPost_Id(contentId);
    }
}
