package org.piet.forumbackend.users.groups.controllers;

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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.UUID;

@RestController
@RequestMapping("${forum.api.prefix}/users/groups/admin")
@RequiredArgsConstructor
public class UserGroupAdminController {
    private final UserService userService;
    private final UserGroupService userGroupService;

    @PatchMapping("/name")
    public ResponseEntity<UserGroupDto> changeName(@Valid @RequestBody ChangeUserGroupNameDto dto, Authentication authentication) throws UserNotLoggedInException, AccessDeniedException, NotFoundException {
        User currentUser = userService.getUserFromAuth(authentication);
        UserGroup group = userGroupService.changeName(dto.getId(), dto.getName(), currentUser);

        return ResponseEntity.ok(UserGroupDto.create(group));
    }

    @PatchMapping("/kick")
    public ResponseEntity<?> kickUser(@Valid @RequestBody ModifyMemberUserGroupDto dto, Authentication authentication) throws UserNotLoggedInException, NotFoundException, AccessDeniedException {
        User currentUser = userService.getUserFromAuth(authentication);
        User member = userService.getUserById(dto.getGetUserDto().getId());
        userGroupService.removeMember(dto.getId(), member, currentUser);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/candidates/accept")
    public ResponseEntity<?> acceptCandidate(@Valid @RequestBody ModifyMemberUserGroupDto dto, Authentication authentication) throws UserNotLoggedInException, NotFoundException, AccessDeniedException {
        User currentUser = userService.getUserFromAuth(authentication);
        User member = userService.getUserById(dto.getGetUserDto().getId());

        userGroupService.addMember(dto.getId(), member, currentUser);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/candidates/reject")
    public ResponseEntity<?> rejectCandidate(@Valid @RequestBody ModifyMemberUserGroupDto dto, Authentication authentication) throws UserNotLoggedInException, NotFoundException, AccessDeniedException {
        User currentUser = userService.getUserFromAuth(authentication);
        User member = userService.getUserById(dto.getGetUserDto().getId());

        userGroupService.rejectMemberCandidate(dto.getId(), member, currentUser);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{groupId}/resign")
    public ResponseEntity<?> resignFromAdmin(@PathVariable UUID groupId, Authentication authentication) throws UserNotLoggedInException, AccessDeniedException, NotFoundException {
        User currentUser = userService.getUserFromAuth(authentication);
        userGroupService.resignAdmin(groupId, currentUser);

        return ResponseEntity.ok().build();
    }
}
