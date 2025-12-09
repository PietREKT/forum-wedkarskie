package org.piet.forumbackend.users.core.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.pagination.PageDto;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.piet.forumbackend.users.core.dtos.requests.GetUserDto;
import org.piet.forumbackend.users.core.dtos.requests.admins.PunishUserDto;
import org.piet.forumbackend.users.core.dtos.responses.ListUserDto;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;
import org.piet.forumbackend.users.core.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${forum.api.prefix}/mod/users")
@RequiredArgsConstructor
public class ModController {

    private final UserService userService;

    @PatchMapping("/mute")
    public ResponseEntity<?> muteUser(@Valid @RequestBody PunishUserDto dto) throws UserNotLoggedInException, NotFoundException {
        userService.muteUser(dto.getId(), dto.getPunishedUntil(), dto.getReason(), userService.getCurrentUser());

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/unmute")
    public ResponseEntity<?> muteUser(@Valid @RequestBody GetUserDto dto) throws NotFoundException {
        userService.unmuteUser(dto.getId());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/muted")
    public ResponseEntity<PageDto<ListUserDto>> getMutedUsers(PaginationDto pagination){
        var users = userService.getMutedUsers(pagination);

        return ResponseEntity.ok(PageDto.of(users));
    }
}
