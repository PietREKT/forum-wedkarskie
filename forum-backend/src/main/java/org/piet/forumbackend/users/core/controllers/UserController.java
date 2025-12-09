package org.piet.forumbackend.users.core.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.events.dtos.responses.EventDto;
import org.piet.forumbackend.events.services.EventsService;
import org.piet.forumbackend.fishing_spots.dtos.FishingSpotListDto;
import org.piet.forumbackend.fishing_spots.services.FishingSpotService;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.pagination.PageDto;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.piet.forumbackend.users.core.dtos.UsersDtoMapper;
import org.piet.forumbackend.users.core.dtos.responses.ListUserDto;
import org.piet.forumbackend.users.core.dtos.responses.UserDto;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;
import org.piet.forumbackend.users.core.services.UserServiceImpl;
import org.piet.forumbackend.users.groups.dtos.responses.ListUserGroupDto;
import org.piet.forumbackend.users.groups.services.UserGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${forum.api.prefix}/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints for user operations")
public class UserController {
    private final UserServiceImpl userService;
    private final EventsService eventsService;
    private final UserGroupService userGroupService;
    private final FishingSpotService fishingSpotService;

    @GetMapping("/search")
    public ResponseEntity<List<ListUserDto>> getUsersByUsername(@RequestParam("q") String query){
        var suggestions = userService.searchByUsernamePrefix(query);
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserInfoById(@PathVariable UUID userId) throws NotFoundException {
        return ResponseEntity.ok(UsersDtoMapper.toUserDto(userService.getUserById(userId)));
    }

    @GetMapping("/{userId}/groups")
    public ResponseEntity<PageDto<ListUserGroupDto>> getUserGroupsById(@PathVariable UUID userId, PaginationDto pagination) throws UserNotLoggedInException {
        var page = userGroupService.getGroupsByMember(userId, pagination);
        return ResponseEntity.ok(PageDto.of(page));
    }


    @GetMapping("/{userId}/events/created")
    public ResponseEntity<PageDto<EventDto>> getEventsCreatedByUser(@PathVariable UUID userId, PaginationDto pagination){
        return ResponseEntity.ok(eventsService.getEventsCreatedByUser(userId, pagination));
    }

    @GetMapping("/{userId}/events/upcoming")
    public ResponseEntity<PageDto<EventDto>> getUpcomingEventsForUser(@PathVariable UUID userId, PaginationDto pagination){
        return ResponseEntity.ok(eventsService.getUpcomingEventsForUser(userId, pagination));
    }

    @GetMapping("/{userId}/spots/favourites")
    public ResponseEntity<PageDto<FishingSpotListDto>> getUserFavouriteSpots(@PathVariable UUID userId, PaginationDto pagination) throws UserNotLoggedInException {
        var dtos = fishingSpotService.getUserFavourites(userId, pagination);

        return ResponseEntity.ok(PageDto.of(dtos));
    }
}
