package org.piet.forumbackend.content.reports.services;

import org.piet.forumbackend.content.comments.Comment;
import org.piet.forumbackend.content.comments.dtos.CommentDtoMapper;
import org.piet.forumbackend.content.reports.ReportReason;
import org.piet.forumbackend.content.reports.dtos.CommentReportSummaryDto;
import org.piet.forumbackend.content.reports.entities.CommentReport;
import org.piet.forumbackend.content.reports.entities.ContentReport;
import org.piet.forumbackend.content.reports.repositories.CommentReportRepository;
import org.piet.forumbackend.exceptions.NotFoundException;
import org.piet.forumbackend.users.entities.User;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentReportService implements ContentReportServiceInt<CommentReport, Comment> {
    private final CommentReportRepository commentReportRepository;
    private final MessageSource messageSource;

    public CommentReportService(CommentReportRepository commentReportRepository, MessageSource messageSource) {
        this.commentReportRepository = commentReportRepository;
        this.messageSource = messageSource;
    }

    @Override
    public CommentReport getReportById(Long id) throws NotFoundException {
        return commentReportRepository.findById(id).orElseThrow(() -> new NotFoundException(
                messageSource.getMessage("errors.comments.reports.not_found",
                        new Object[]{id},
                        LocaleContextHolder.getLocale()
                )
        ));
    }

    @Override
    public CommentReport createReport(ReportReason reportReason, Comment reported, User reportedBy) {
        CommentReport commentReport = new CommentReport();
        commentReport.setReportedComment(reported);
        commentReport.setReason(reportReason);
        commentReport.setReportedBy(reportedBy);
        return commentReportRepository.save(commentReport);
    }

    @Override
    public void deleteReport(Long id) {
        commentReportRepository.deleteById(id);
    }

    @Override
    public List<User> getUsersWhoReportedContent(Long contentId) throws NotFoundException {
        return commentReportRepository.findByReportedComment_Id(contentId).stream().map(ContentReport::getReportedBy).toList();
    }

    @Override
    public List<CommentReport> getReportsByContentId(Long contentId) {
        return commentReportRepository.findByReportedComment_Id(contentId);
    }

    public List<CommentReportSummaryDto> getAggregatedReports(Pageable pageable){
        List<CommentReport> allReports = commentReportRepository.findAll(pageable).getContent();
        Map<Long, List<CommentReport>> groupedReports = allReports.stream().collect(Collectors.groupingBy(r -> r.getReportedComment().getId()));

        return groupedReports.keySet().stream()
                .map(commentId -> {
                    List<CommentReport> commentReports = getReportsByContentId(commentId);

                    Map<ReportReason, Long> reasonsMapped = commentReports.stream().
                            collect(Collectors.groupingBy(ContentReport::getReason, Collectors.counting()));
                    return new CommentReportSummaryDto(
                            CommentDtoMapper.toCommentDto(commentReports.getFirst().getReportedComment()),
                            (long) commentReports.size(),
                            reasonsMapped);
                }).toList();
    }
}
