package org.piet.forumbackend.users.controllers;

import org.piet.forumbackend.content.reports.dtos.CommentReportSummaryDto;
import org.piet.forumbackend.content.reports.services.CommentReportService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${forum.api.prefix}/mod")
public class ModController {
    private final CommentReportService commentReportService;

    public ModController(CommentReportService commentReportService) {
        this.commentReportService = commentReportService;
    }

    @GetMapping("/reports/summary")
    ResponseEntity<List<CommentReportSummaryDto>> getCommentSummary(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "count", required = false, defaultValue = "30") int itemsPerPage
    ){
        Pageable pageable = PageRequest.of(page, itemsPerPage, Sort.by("createdAt").descending());
        List<CommentReportSummaryDto> dtos = commentReportService.getAggregatedReports(pageable);

        return ResponseEntity.ok(dtos);
    }
}
