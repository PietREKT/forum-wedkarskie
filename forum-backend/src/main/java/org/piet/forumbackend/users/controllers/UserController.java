package org.piet.forumbackend.users.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.users.UserService;
import org.piet.forumbackend.users.dtos.UserDto;
import org.piet.forumbackend.users.dtos.UsersDtoMapper;
import org.piet.forumbackend.users.exceptions.UserNotLoggedInException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${forum.api.prefix}/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints for user operations")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Get user info")
    @GetMapping("/me")
    public ResponseEntity<UserDto> getUserInfo(Authentication auth) throws UserNotLoggedInException {
        UserDto dto = UsersDtoMapper.toUserDto(userService.getUserFromAuth(auth));
        return ResponseEntity.ok(dto);

    }
}
