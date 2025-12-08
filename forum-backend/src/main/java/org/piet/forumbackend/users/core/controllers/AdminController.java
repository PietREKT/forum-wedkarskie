package org.piet.forumbackend.users.core.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.pagination.PageDto;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.piet.forumbackend.users.core.dtos.requests.GetUserDto;
import org.piet.forumbackend.users.core.dtos.requests.admins.ChangeUserRoleDto;
import org.piet.forumbackend.users.core.dtos.requests.admins.PunishUserDto;
import org.piet.forumbackend.users.core.dtos.responses.ListUserDto;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;
import org.piet.forumbackend.users.core.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("${forum.api.prefix}/admin/users")
@Tag(name = "Admin", description = "Endpoints for site management.")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;

    @PostMapping("/ban")
    public ResponseEntity<?> banUser(@Valid @RequestBody PunishUserDto dto) throws UserNotLoggedInException, NotFoundException {
        if (dto.getPunishedUntil() == null)
            userService.permBanUser(dto.getId(), dto.getReason(), userService.getCurrentUser());
        else
            userService.banUser(dto.getId(), dto.getPunishedUntil(), dto.getReason(), userService.getCurrentUser());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unban")
    public ResponseEntity<?> unbanUser(@Valid @RequestBody GetUserDto dto) throws NotFoundException {
        userService.unbanUser(dto.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/banned")
    public ResponseEntity<PageDto<ListUserDto>> getBannedUsers(PaginationDto pagination){
        var users = userService.getBannedUsers(pagination);

        return ResponseEntity.ok(PageDto.createDto(users));
    }

    @GetMapping("/list")
    public ResponseEntity<PageDto<ListUserDto>> getAllUsers(PaginationDto pagination){
        return ResponseEntity.ok(PageDto.createDto(userService.getAllUsers(pagination)));
    }

    @PostMapping("/{userId}/roles/change")
    public ResponseEntity<?> changeUserRole(@PathVariable UUID userId, @Valid @RequestBody ChangeUserRoleDto dto) throws UserNotLoggedInException, NotFoundException {
        userService.changeUserRole(userId, dto.getRole());

        return ResponseEntity.noContent().build();
    }
}
