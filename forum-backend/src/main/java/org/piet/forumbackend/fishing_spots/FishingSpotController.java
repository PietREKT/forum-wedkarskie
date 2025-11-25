package org.piet.forumbackend.fishing_spots;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.piet.forumbackend.exceptions.BadRequestException;
import org.piet.forumbackend.exceptions.UnauthorizedAccessException;
import org.piet.forumbackend.fish.FishService;
import org.piet.forumbackend.fish.dtos.GetFishDto;
import org.piet.forumbackend.fish.entities.Fish;
import org.piet.forumbackend.fishing_spots.dtos.CreateFishingSpotDto;
import org.piet.forumbackend.fishing_spots.dtos.FishingSpotDto;
import org.piet.forumbackend.fishing_spots.dtos.FishingSpotListDto;
import org.piet.forumbackend.fishing_spots.exceptions.FishNotFoundException;
import org.piet.forumbackend.fishing_spots.exceptions.LocationDtoIncompleteException;
import org.piet.forumbackend.fishing_spots.exceptions.LocationNotFoundException;
import org.piet.forumbackend.pagination.PaginationDto;
import org.piet.forumbackend.users.UserService;
import org.piet.forumbackend.users.entities.User;
import org.piet.forumbackend.users.exceptions.UserNotLoggedInException;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
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
    private final UserService userService;
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
    public ResponseEntity<FishingSpotDto> createSpot(@RequestBody CreateFishingSpotDto dto, Authentication auth) throws UserNotLoggedInException, FishNotFoundException, UnauthorizedAccessException, LocationDtoIncompleteException, BadRequestException, IOException, LocationNotFoundException {
        User u = userService.getUserFromAuth(auth);

        List<User> managers = dto.getManagers() != null ?
                dto.getManagers()
                        .stream()
                        .map(m -> userService.getUserByUsername(m.getUsername()))
                        .collect(Collectors.toList()) :
                new ArrayList<>();
        List<Fish> fish;
        if (dto.getFish().stream().allMatch(f -> f.getId() != null)){
            fish = dto.getFish()
                    .stream()
                    .map(GetFishDto::getId)
                    .map(fishService::getFishByIdOptional)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
        } else {
            throw new BadRequestException("fish needs id or name"); //placeholder
        }

        if (fish.size() != dto.getFish().size()){
            log.warn("Fish are not complete :(");
            //TODO throw if not all fish got recognized
        }

        var spot = fishingSpotService.createFishingSpot(
                dto.getName(),
                dto.getDescription(),
                dto.getType(),
                managers,
                fish,
                dto.getLocationDto(),
                u
//                dto.getStatue()
        );

        return ResponseEntity.ok(FishingSpotDto.create(spot));
    }
}
