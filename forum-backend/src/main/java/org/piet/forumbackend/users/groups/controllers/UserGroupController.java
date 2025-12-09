package org.piet.forumbackend.users.groups.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.content.dtos.responses.content.ContentDto;
import org.piet.forumbackend.content.services.ContentService;
import org.piet.forumbackend.events.dtos.responses.EventDto;
import org.piet.forumbackend.events.services.EventsService;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.pagination.PageDto;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.piet.forumbackend.users.core.dtos.requests.GetUserDto;
import org.piet.forumbackend.users.core.dtos.responses.ListUserDto;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;
import org.piet.forumbackend.users.core.services.UserService;
import org.piet.forumbackend.users.groups.dtos.requests.CreateUserGroupDto;
import org.piet.forumbackend.users.groups.dtos.requests.ModifyMemberUserGroupDto;
import org.piet.forumbackend.users.groups.dtos.responses.ListUserGroupDto;
import org.piet.forumbackend.users.groups.dtos.responses.UserGroupDto;
import org.piet.forumbackend.users.groups.entities.UserGroup;
import org.piet.forumbackend.users.groups.services.UserGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("${forum.api.prefix}/users/groups")
@RequiredArgsConstructor
public class UserGroupController {
    private final UserService userService;
    private final UserGroupService userGroupService;
    private final EventsService eventsService;
    private final ContentService contentService;

    @PostMapping("/create")
    public ResponseEntity<UserGroupDto> createGroup(@Valid @RequestBody CreateUserGroupDto dto, Authentication authentication) throws UserNotLoggedInException {
        User currentUser = userService.getUserFromAuth(authentication);
        var members = dto.getMembers()
                .stream()
                .map(GetUserDto::getId)
                .map(userService::getUserByIdOpt)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
        UserGroup group = userGroupService.createUserGroup(currentUser, members, dto.getName());

        return ResponseEntity.ok(UserGroupDto.create(group));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ListUserGroupDto>> searchGroupsByName(@RequestParam("q") String query){
        var dtos = userGroupService.getByName(query);

        return ResponseEntity.ok(dtos);
    }

    @PatchMapping("/{groupId}/invite")
    public ResponseEntity<?> inviteMember(@PathVariable UUID groupId,
                                          @Valid @RequestBody ModifyMemberUserGroupDto dto,
                                          Authentication authentication) throws UserNotLoggedInException, NotFoundException, AccessDeniedException {
        User currentUser = userService.getUserFromAuth(authentication);
        User modifyUser = userService.getUserById(dto.getUserId());

        userGroupService.addMemberCandidate(groupId, modifyUser, currentUser);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{groupId}/leave")
    public ResponseEntity<?> leaveGroup(@PathVariable UUID groupId, Authentication authentication) throws UserNotLoggedInException, AccessDeniedException, NotFoundException {
        User currentUser = userService.getUserFromAuth(authentication);

        userGroupService.leave(groupId, currentUser);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<UserGroupDto> getGroupById(@PathVariable UUID groupId) throws NotFoundException {
        UserGroup group = userGroupService.getById(groupId);

        return ResponseEntity.ok(UserGroupDto.create(group));
    }

    @GetMapping("/{groupId}/events")
    public ResponseEntity<PageDto<EventDto>> getEventsForGroup(@PathVariable UUID groupId, PaginationDto pagination){
        return ResponseEntity.ok(
                eventsService.getEventsForGroup(groupId, pagination)
        );
    }

    @GetMapping("/{groupId}/candidates")
    public ResponseEntity<PageDto<ListUserDto>> getGroupCandidates(@PathVariable UUID groupId, PaginationDto pagination) {
        var page = userGroupService.getMemberCandidates(groupId, pagination);

        return ResponseEntity.ok(PageDto.of(page));
    }

    @GetMapping("/{groupId}/posts")
    public ResponseEntity<PageDto<ContentDto>> getRecentPosts(@PathVariable UUID groupId, PaginationDto pagination){
        var page = contentService.getRecentPostsByGroup(groupId, pagination);

        return ResponseEntity.ok(PageDto.of(page));
    }
}
