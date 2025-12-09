package org.piet.forumbackend.users.core.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.events.dtos.responses.EventDto;
import org.piet.forumbackend.events.services.EventsService;
import org.piet.forumbackend.globals.pagination.PageDto;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.piet.forumbackend.users.core.dtos.UsersDtoMapper;
import org.piet.forumbackend.users.core.dtos.responses.UserDto;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;
import org.piet.forumbackend.users.core.services.UserService;
import org.piet.forumbackend.users.groups.dtos.responses.ListUserGroupDto;
import org.piet.forumbackend.users.groups.services.UserGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
@RestController
@RequestMapping("${forum.api.prefix}/users/me")
@RequiredArgsConstructor
public class MeController {
    private final UserService userService;
    private final UserGroupService userGroupService;
    private final EventsService eventsService;

    @Operation(summary = "Get user info")
    @GetMapping
    public ResponseEntity<UserDto> getUserInfo(Authentication auth) throws UserNotLoggedInException {
        UserDto dto = UsersDtoMapper.toUserDto(userService.getUserFromAuth(auth));
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/groups")
    public ResponseEntity<PageDto<ListUserGroupDto>> getUserGroups(PaginationDto pagination) throws UserNotLoggedInException {
        var page = userGroupService.getGroupsByMember(userService.getCurrentUser().getId(), pagination);
        return ResponseEntity.ok(PageDto.of(page));
    }

    @GetMapping("/groups/invites")
    public ResponseEntity<PageDto<ListUserGroupDto>> getInvitesToGroups(PaginationDto paginationDto) throws UserNotLoggedInException {
        var page = userGroupService.getGroupsByCandidateId(userService.getCurrentUser().getId(), paginationDto);
        return ResponseEntity.ok(PageDto.of(page));
    }

    @GetMapping("/events/created")
    public ResponseEntity<PageDto<EventDto>> getEventsCreatedByCurrentUser(PaginationDto pagination) throws UserNotLoggedInException {
        UUID userId = userService.getCurrentUser().getId();
        return ResponseEntity.ok(eventsService.getEventsCreatedByUser(userId, pagination));
    }

    @GetMapping("/events/upcoming")
    public ResponseEntity<PageDto<EventDto>> getUpcomingEventsForCurrentUser(PaginationDto pagination) throws UserNotLoggedInException {
        UUID userId = userService.getCurrentUser().getId();
        return ResponseEntity.ok(eventsService.getUpcomingEventsForUser(userId, pagination));
    }
}
