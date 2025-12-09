package org.piet.forumbackend.reports.controllers;

import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.content.entities.Content;
import org.piet.forumbackend.content.services.ContentService;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.pagination.PageDto;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.piet.forumbackend.reports.dtos.content.responses.ContentReportDto;
import org.piet.forumbackend.reports.dtos.content.responses.HotReportedContentDto;
import org.piet.forumbackend.reports.services.ContentReportService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("${forum.api.prefix}/admin/reports")
@RequiredArgsConstructor
public class ContentReportAdminController {
    private final ContentReportService contentReportService;
    private final ContentService contentService;

    @GetMapping("/summary")
    ResponseEntity<PageDto<HotReportedContentDto>> getReportsSummary(
            @ParameterObject PaginationDto paginationDto,
            @RequestParam(name = "amount", required = false, defaultValue = "1") Long amount,
            @RequestParam(name = "unit", required = false, defaultValue = "WEEKS") ChronoUnit unit
    ) {
        var dtos = contentReportService.getRecentlyReportedContent(amount, unit, paginationDto);

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{contentId}")
    ResponseEntity<ContentReportDto> getContentReports(@PathVariable Long contentId) throws NotFoundException {
        Content content = contentService.getContentById(contentId);
        var dto = contentReportService.getReportSummary(content);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{contentId}/dismiss")
    public ResponseEntity<?> dismissContentReports(@PathVariable Long contentId){
        contentReportService.dismissReports(contentId);
        return ResponseEntity.noContent().build();
    }
}
