package org.piet.forumbackend.content.reports.controllers;

import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.content.entities.Content;
import org.piet.forumbackend.content.reports.dtos.ContentReportDto;
import org.piet.forumbackend.content.reports.dtos.HotReportedContentDto;
import org.piet.forumbackend.content.reports.entities.enums.TimeUnitInput;
import org.piet.forumbackend.content.reports.services.ContentReportService;
import org.piet.forumbackend.content.services.ContentService;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${forum.api.prefix}/admin/reports")
@RequiredArgsConstructor
public class ContentReportAdminController {
    ContentReportService contentReportService;
    ContentService contentService;

    @GetMapping("/reports/summary")
    ResponseEntity<List<HotReportedContentDto>> getReportsSummary(
            @ParameterObject PaginationDto paginationDto,
            @RequestParam(name = "amount", required = false, defaultValue = "1") Long amount,
            @RequestParam(name = "unit", required = false, defaultValue = "WEEKS") TimeUnitInput unit
    ) {
        var dtos = contentReportService.getRecentlyReportedContent(amount, unit.map(), paginationDto.getPage(), paginationDto.getSize());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/reports/{contentId}")
    ResponseEntity<ContentReportDto> getContentReports(@PathVariable Long contentId) throws NotFoundException {
        Content content = contentService.getContentById(contentId);
        var dto = contentReportService.getReportSummary(content);
        return ResponseEntity.ok(dto);
    }
}
