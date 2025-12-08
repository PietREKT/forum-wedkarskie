package org.piet.forumbackend.fishing_spots.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.fishing_spots.dtos.requests.CreateFishingSpotDto;
import org.piet.forumbackend.fishing_spots.dtos.responses.FishingSpotDto;
import org.piet.forumbackend.fishing_spots.exceptions.FishingSpotNotFoundException;
import org.piet.forumbackend.fishing_spots.exceptions.LocationDtoIncompleteException;
import org.piet.forumbackend.fishing_spots.exceptions.LocationNotFoundException;
import org.piet.forumbackend.fishing_spots.services.FishingSpotService;
import org.piet.forumbackend.globals.exceptions.UnauthorizedAccessException;
import org.piet.forumbackend.globals.pagination.PageDto;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("${forum.api.prefix}/admin/spots")
@RequiredArgsConstructor
@Tag(name = "Admin controller - fishing spots", description = "Endpoints for admin's spot management.")
public class FishingSpotAdminController {
    private final FishingSpotService fishingSpotService;

    @GetMapping("/unverified")
    public ResponseEntity<PageDto<FishingSpotDto>> getUnverified(PaginationDto pagination){
        var spots = fishingSpotService.getUnverified(pagination);

        return ResponseEntity.ok(PageDto.createDto(spots));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<?> acceptFishingSpot(@PathVariable Long id) throws FishingSpotNotFoundException {
        fishingSpotService.markFishingSpotAsVerified(id);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<?> rejectFishingSpot(@PathVariable Long id) throws FishingSpotNotFoundException {
        fishingSpotService.markFishingSpotAsRejected(id);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/revoke")
    public ResponseEntity<?> revokeFishingSpotVerification(@PathVariable Long id) throws FishingSpotNotFoundException {
        fishingSpotService.revokeFishingSpotReview(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<FishingSpotDto> addFishingSpot(@Valid @RequestBody CreateFishingSpotDto createFishingSpotDto) throws UserNotLoggedInException, UnauthorizedAccessException, LocationDtoIncompleteException, IOException, LocationNotFoundException {
        FishingSpotDto dto = FishingSpotDto.create(fishingSpotService.createFishingSpot(createFishingSpotDto));

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
}
