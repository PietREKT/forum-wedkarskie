package org.piet.forumbackend.users.groups.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;
import org.piet.forumbackend.users.core.services.UserService;
import org.piet.forumbackend.users.groups.dtos.requests.ModifyMemberUserGroupDto;
import org.piet.forumbackend.users.groups.dtos.requests.TransferOwnershipDto;
import org.piet.forumbackend.users.groups.services.UserGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("${forum.api.prefix}/users/groups/owner")
@RequiredArgsConstructor
public class UserGroupOwnerController {
    private final UserService userService;
    private final UserGroupService userGroupService;

    @PatchMapping("/admins/add")
    public ResponseEntity<?> addAdmin(@Valid @RequestBody ModifyMemberUserGroupDto dto) throws UserNotLoggedInException, NotFoundException, AccessDeniedException {
        User currentUser = userService.getCurrentUser();
        User newAdmin = userService.getUserById(dto.getGetUserDto().getId());

        userGroupService.addAdmin(dto.getId(), newAdmin, currentUser);
        return ResponseEntity.ok().build();
    }
    @PatchMapping("/admins/remove")
    public ResponseEntity<?> removeAdmin(@Valid @RequestBody ModifyMemberUserGroupDto dto) throws UserNotLoggedInException, NotFoundException, AccessDeniedException {
        User currentUser = userService.getCurrentUser();
        User newAdmin = userService.getUserById(dto.getGetUserDto().getId());

        userGroupService.removeAdmin(dto.getId(), newAdmin, currentUser);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/transfer")
    public ResponseEntity<?> transferOwnership(@Valid @RequestBody TransferOwnershipDto dto) throws UserNotLoggedInException, NotFoundException, AccessDeniedException {
        User currentUser = userService.getCurrentUser();
        User newAdmin = userService.getUserById(dto.getGetUserDto().getId());

        userGroupService.transferOwnership(dto.getId(), newAdmin, currentUser, dto.getRemoveFromAdmins());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<?> deleteGroup(@PathVariable UUID groupId) throws UserNotLoggedInException, AccessDeniedException, NotFoundException {
        User currentUser = userService.getCurrentUser();
        userGroupService.deleteUserGroup(groupId, currentUser);

        return ResponseEntity.ok().build();
    }
}
