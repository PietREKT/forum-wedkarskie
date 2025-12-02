package org.piet.forumbackend.users.groups.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.users.core.dtos.requests.GetUserDto;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;
import org.piet.forumbackend.users.core.services.UserService;
import org.piet.forumbackend.users.groups.dtos.requests.CreateUserGroupDto;
import org.piet.forumbackend.users.groups.dtos.requests.ModifyMemberUserGroupDto;
import org.piet.forumbackend.users.groups.dtos.responses.UserGroupDto;
import org.piet.forumbackend.users.groups.entities.UserGroup;
import org.piet.forumbackend.users.groups.services.UserGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("${forum.api.prefix}/users/groups")
@RequiredArgsConstructor
public class UserGroupController {
    private final UserService userService;
    private final UserGroupService userGroupService;

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

    @PatchMapping("/invite")
    public ResponseEntity<?> inviteMember(@Valid @RequestBody ModifyMemberUserGroupDto dto, Authentication authentication) throws UserNotLoggedInException, NotFoundException, AccessDeniedException {
        User currentUser = userService.getUserFromAuth(authentication);
        User modifyUser = userService.getUserById(dto.getGetUserDto().getId());

        userGroupService.addMemberCandidate(dto.getId(), modifyUser, currentUser);

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
}
