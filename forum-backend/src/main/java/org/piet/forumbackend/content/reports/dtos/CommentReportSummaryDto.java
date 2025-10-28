package org.piet.forumbackend.content.reports.dtos;

import lombok.Value;
import org.piet.forumbackend.content.comments.dtos.CommentDto;
import org.piet.forumbackend.content.reports.ReportReason;

import java.io.Serializable;
import java.util.Map;

@Value
public class CommentReportSummaryDto implements Serializable {
    CommentDto comment;
    Long totalReports;
    Map<ReportReason, Long> reasonSummary;
}
