package org.piet.forumbackend.content.reports.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.content.core.entities.Content;
import org.piet.forumbackend.content.core.services.ContentService;
import org.piet.forumbackend.content.reports.dtos.responses.ContentReportDto;
import org.piet.forumbackend.content.reports.dtos.responses.HotReportedContentDto;
import org.piet.forumbackend.content.reports.services.ContentReportService;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.pagination.PageDto;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("${forum.api.prefix}/admin/reports/content")
@RequiredArgsConstructor
@Tag(name = "Admin - reports", description = "Endpoints for admin content report management.")
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
