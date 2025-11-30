package org.piet.forumbackend.fishing_spots.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.piet.forumbackend.fish.dtos.GetFishDto;
import org.piet.forumbackend.fish.entities.Fish;
import org.piet.forumbackend.fish.services.FishService;
import org.piet.forumbackend.fishing_spots.dtos.CreateFishingSpotDto;
import org.piet.forumbackend.fishing_spots.dtos.FishingSpotDto;
import org.piet.forumbackend.fishing_spots.dtos.FishingSpotListDto;
import org.piet.forumbackend.fishing_spots.exceptions.LocationDtoIncompleteException;
import org.piet.forumbackend.fishing_spots.exceptions.LocationNotFoundException;
import org.piet.forumbackend.fishing_spots.services.FishingSpotService;
import org.piet.forumbackend.globals.exceptions.BadRequestException;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.exceptions.UnauthorizedAccessException;
import org.piet.forumbackend.globals.pagination.PageDto;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;
import org.piet.forumbackend.users.core.services.UserServiceImpl;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/spots")
@Tag(name = "Fishing Spot Controller", description = "Endpoints for fishing spots management")
@RequiredArgsConstructor
public class FishingSpotController {

    private final FishingSpotService fishingSpotService;
    private final UserServiceImpl userService;
    private final FishService fishService;

    @GetMapping("radius")
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
        User u = userService.getUserFromAuth(auth);
        List<User> managers =
                dto.getManagers()
                        .stream()
                        .map(m -> userService.getUserByIdOpt(m.getId()))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
        List<Fish> fish;
        if (dto.getFish().stream().allMatch(f -> f.getId() != null)) {
            fish = dto.getFish()
                    .stream()
                    .map(GetFishDto::getId)
                    .map(fishService::getFishByIdOptional)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
        } else if (dto.getFish().stream().allMatch(f -> f.getName() != null)){
            fish = dto.getFish()
                    .stream()
                    .map(GetFishDto::getName)
                    .map(fishService::getFishByNameOptional)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
        }
        else {
            throw new BadRequestException("fish needs id or name"); //placeholder
        }

        if (fish.size() != dto.getFish().size()) {
            log.warn("Fish are not complete :(");
            throw new BadRequestException("Not all fish were found");
        }
        if (managers.size() != dto.getManagers().size()) {
            log.warn("Fish are not complete :(");
            throw new BadRequestException("Not all fish were found");
        }

        var spot = fishingSpotService.createFishingSpot(
                dto.getName(),
                dto.getDescription(),
                dto.getType(),
                managers,
                fish,
                dto.getLocationDto(),
                u
        );

        return ResponseEntity.ok(FishingSpotDto.create(spot));
    }

    @GetMapping
    public ResponseEntity<PageDto<FishingSpotListDto>> getSpots(PaginationDto dto) {
        var spots = fishingSpotService.getFishingSpots(dto)
                .map(FishingSpotListDto::create);
        return ResponseEntity.ok(PageDto.createDto(spots));
    }
}
