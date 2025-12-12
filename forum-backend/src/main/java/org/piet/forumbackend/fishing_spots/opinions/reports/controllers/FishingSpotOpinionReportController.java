package org.piet.forumbackend.fishing_spots.opinions.reports.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.fishing_spots.opinions.reports.dtos.requests.CreateFishingSpotOpinionReportDto;
import org.piet.forumbackend.fishing_spots.opinions.reports.services.FishingSpotOpinionReportService;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${forum.api.prefix}/spots/opinions/reports")
@RequiredArgsConstructor
@Tag(name = "Opinion Report Controller", description = "Endpoints for opinion reports")
public class FishingSpotOpinionReportController {
    private final FishingSpotOpinionReportService fishingSpotOpinionReportService;

    @PostMapping
    public ResponseEntity<?> createOpinionReport(@Valid @RequestBody CreateFishingSpotOpinionReportDto dto) throws NotFoundException {
        fishingSpotOpinionReportService.createReport(dto);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
