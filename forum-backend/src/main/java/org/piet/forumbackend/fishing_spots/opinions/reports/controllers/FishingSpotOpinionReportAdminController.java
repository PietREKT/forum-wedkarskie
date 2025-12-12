package org.piet.forumbackend.fishing_spots.opinions.reports.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.fishing_spots.opinions.reports.dtos.responses.HotReportedOpinionsDto;
import org.piet.forumbackend.fishing_spots.opinions.reports.dtos.responses.OpinionReportDto;
import org.piet.forumbackend.fishing_spots.opinions.reports.services.FishingSpotOpinionReportService;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.pagination.PageDto;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("${forum.api.prefix}/admin/reports/opinions")
@RequiredArgsConstructor
@Tag(name = "Admin - Opinions", description = "Endpoints for admin to manage opinion reports")
public class FishingSpotOpinionReportAdminController {
    private final FishingSpotOpinionReportService fishingSpotOpinionReportService;

    @GetMapping("/summary")
    public ResponseEntity<PageDto<HotReportedOpinionsDto>> getHotOpinionsSummary(
            @RequestParam(defaultValue = "1") Long unitAmount,
            @RequestParam(defaultValue = "WEEKS") ChronoUnit unit,
            PaginationDto pagination
            ){
        var dtos = fishingSpotOpinionReportService.getRecentlyReportedOpinions(unitAmount, unit, pagination);

        return ResponseEntity.ok(PageDto.of(dtos));
    }

    @GetMapping("/{opinionId}")
    public ResponseEntity<OpinionReportDto> getOpinionReportSummary(@PathVariable Long opinionId) throws NotFoundException {
        var dto = fishingSpotOpinionReportService.getReportSummary(opinionId);

        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{opinionId}/dismiss")
    public ResponseEntity<?> dismissReportsForOpinion(@PathVariable Long opinionId){
        fishingSpotOpinionReportService.dismissReports(opinionId);

        return ResponseEntity.noContent().build();
    }
}
