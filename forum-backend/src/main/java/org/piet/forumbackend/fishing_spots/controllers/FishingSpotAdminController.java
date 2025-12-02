package org.piet.forumbackend.fishing_spots.controllers;

import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.fishing_spots.dtos.FishingSpotDto;
import org.piet.forumbackend.fishing_spots.exceptions.FishingSpotNotFoundException;
import org.piet.forumbackend.fishing_spots.services.FishingSpotService;
import org.piet.forumbackend.globals.pagination.PageDto;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${forum.api.prefix}/admin/spots")
@RequiredArgsConstructor
public class FishingSpotAdminController {
    private final FishingSpotService fishingSpotService;

    @GetMapping("/unverified")
    public ResponseEntity<PageDto<FishingSpotDto>> getUnverified(PaginationDto pagination){
        var spots = fishingSpotService.getUnverified(pagination);

        return ResponseEntity.ok(PageDto.createDto(spots));
    }

    @PatchMapping("/{id}/accept")
    public ResponseEntity<?> acceptFishingSpot(@PathVariable Long id) throws FishingSpotNotFoundException {
        fishingSpotService.markFishingSpotAsVerified(id);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> rejectFishingSpot(@PathVariable Long id) throws FishingSpotNotFoundException {
        fishingSpotService.markFishingSpotAsRejected(id);

        return ResponseEntity.ok().build();
    }
}
