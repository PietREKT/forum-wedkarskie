package org.piet.forumbackend.fishing_spots.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.piet.forumbackend.content.entities.enums.VerificationStatus;
import org.piet.forumbackend.events.dtos.responses.EventDto;
import org.piet.forumbackend.events.services.EventsService;
import org.piet.forumbackend.fish.services.FishService;
import org.piet.forumbackend.fishing_spots.dtos.FishingSpotListDto;
import org.piet.forumbackend.fishing_spots.dtos.requests.CreateFishingSpotDto;
import org.piet.forumbackend.fishing_spots.dtos.responses.FishingSpotDto;
import org.piet.forumbackend.fishing_spots.exceptions.FishingSpotNotFoundException;
import org.piet.forumbackend.fishing_spots.exceptions.LocationDtoIncompleteException;
import org.piet.forumbackend.fishing_spots.exceptions.LocationNotFoundException;
import org.piet.forumbackend.fishing_spots.services.FishingSpotService;
import org.piet.forumbackend.globals.exceptions.BadRequestException;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.exceptions.UnauthorizedAccessException;
import org.piet.forumbackend.globals.pagination.PageDto;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;
import org.piet.forumbackend.users.core.services.UserServiceImpl;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/spots")
@Tag(name = "Fishing Spot Controller", description = "Endpoints for fishing spots management")
@RequiredArgsConstructor
public class FishingSpotController {

    private final FishingSpotService fishingSpotService;
    private final UserServiceImpl userService;
    private final FishService fishService;
    private final EventsService eventsService;

    @GetMapping("/radius")
    public ResponseEntity<List<FishingSpotListDto>> getSpotsInRadius(
            @RequestParam("x") Double x,
            @RequestParam("y") Double y,
            @RequestParam("radiusKm") Integer radius,
            @ParameterObject PaginationDto paginationDto
    ) {
        var dtos = fishingSpotService.getFishingSpotsInRadius(x, y, radius, PageRequest.of(paginationDto.getPage(), paginationDto.getSize()))
                .stream().map(FishingSpotListDto::create).toList();

        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/create")
    public ResponseEntity<FishingSpotDto> createSpot(@Valid @RequestBody CreateFishingSpotDto dto, Authentication auth) throws UserNotLoggedInException, NotFoundException, UnauthorizedAccessException, LocationDtoIncompleteException, BadRequestException, IOException, LocationNotFoundException {
        var spot = fishingSpotService.createFishingSpot(dto);

        return ResponseEntity.ok(FishingSpotDto.create(spot));
    }

    @GetMapping
    public ResponseEntity<PageDto<FishingSpotListDto>> getSpots(PaginationDto dto) {
        var spots = fishingSpotService.getFishingSpotsByStatus(VerificationStatus.ACCEPTED, dto)
                .map(FishingSpotListDto::create);
        return ResponseEntity.ok(PageDto.createDto(spots));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FishingSpotDto> getById(@PathVariable Long id) throws FishingSpotNotFoundException {
        return ResponseEntity.ok(FishingSpotDto.create(fishingSpotService.getFishingSpotById(id)));
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<?> deleteSpot(@PathVariable Long id) throws UserNotLoggedInException, FishingSpotNotFoundException {
        fishingSpotService.deleteFishingSpot(id, userService.getCurrentUser());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{spotId}/events")
    public ResponseEntity<PageDto<EventDto>> getEventsAtSpot(@PathVariable Long spotId, PaginationDto pagination) {
        return ResponseEntity.ok(
                eventsService.getEventsAtSpot(spotId, pagination)
        );
    }

    @GetMapping("/{spotId}/owner")
    public ResponseEntity<?> isOwner(@PathVariable Long spotId) throws UserNotLoggedInException {
        var map = Map.of("fishing_spot:", spotId,
                "is_owner", fishingSpotService.isOwner(userService.getCurrentUser().getId(), spotId)
        );

        return ResponseEntity.ok(map);
    }
}
