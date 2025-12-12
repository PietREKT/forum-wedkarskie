package org.piet.forumbackend.users.groups.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;
import org.piet.forumbackend.users.core.services.UserService;
import org.piet.forumbackend.users.groups.dtos.requests.ChangeUserGroupNameDto;
import org.piet.forumbackend.users.groups.dtos.requests.ModifyMemberUserGroupDto;
import org.piet.forumbackend.users.groups.dtos.responses.UserGroupDto;
import org.piet.forumbackend.users.groups.entities.UserGroup;
import org.piet.forumbackend.users.groups.services.UserGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("${forum.api.prefix}/users/groups/{groupId}/admin")
@RequiredArgsConstructor
@Tag(name = "Groups - Admin", description = "Endpoints for user group's admins.")
public class UserGroupAdminController {
    private final UserService userService;
    private final UserGroupService userGroupService;

    @PatchMapping("/name")
    public ResponseEntity<UserGroupDto> changeName(@PathVariable UUID groupId, @Valid @RequestBody ChangeUserGroupNameDto dto, Authentication authentication) throws UserNotLoggedInException, AccessDeniedException, NotFoundException {
        User currentUser = userService.getUserFromAuth(authentication);
        UserGroup group = userGroupService.changeName(groupId, dto.getName(), currentUser);

        return ResponseEntity.ok(UserGroupDto.create(group));
    }

    @PostMapping("/kick")
    public ResponseEntity<?> kickUser(@PathVariable UUID groupId, @Valid @RequestBody ModifyMemberUserGroupDto dto, Authentication authentication) throws UserNotLoggedInException, NotFoundException, AccessDeniedException {
        User currentUser = userService.getUserFromAuth(authentication);
        User member = userService.getUserById(dto.getUserId());
        userGroupService.removeMember(groupId, member, currentUser);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/candidates/accept")
    public ResponseEntity<?> acceptCandidate(@PathVariable UUID groupId, @Valid @RequestBody ModifyMemberUserGroupDto dto, Authentication authentication) throws UserNotLoggedInException, NotFoundException, AccessDeniedException {
        User currentUser = userService.getUserFromAuth(authentication);
        User member = userService.getUserById(dto.getUserId());

        userGroupService.addMember(groupId, member, currentUser);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/candidates/reject")
    public ResponseEntity<?> rejectCandidate(@PathVariable UUID groupId, @Valid @RequestBody ModifyMemberUserGroupDto dto, Authentication authentication) throws UserNotLoggedInException, NotFoundException, AccessDeniedException {
        User currentUser = userService.getUserFromAuth(authentication);
        User member = userService.getUserById(dto.getUserId());

        userGroupService.rejectMemberCandidate(groupId, member, currentUser);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/resign")
    public ResponseEntity<?> resignFromAdmin(@PathVariable UUID groupId, Authentication authentication) throws UserNotLoggedInException, AccessDeniedException, NotFoundException {
        User currentUser = userService.getUserFromAuth(authentication);
        userGroupService.resignAdmin(groupId, currentUser);

        return ResponseEntity.ok().build();
    }
}
