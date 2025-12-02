package org.piet.forumbackend.users.core.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.users.core.dtos.UsersDtoMapper;
import org.piet.forumbackend.users.core.dtos.responses.UserDto;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;
import org.piet.forumbackend.users.core.services.UserServiceImpl;
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
    private final UserServiceImpl userService;

    @Operation(summary = "Get user info")
    @GetMapping("/me")
    public ResponseEntity<UserDto> getUserInfo(Authentication auth) throws UserNotLoggedInException {
        UserDto dto = UsersDtoMapper.toUserDto(userService.getUserFromAuth(auth));
        return ResponseEntity.ok(dto);

    }
}
