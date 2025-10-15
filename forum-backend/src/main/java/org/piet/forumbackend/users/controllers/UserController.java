package org.piet.forumbackend.users.controllers;

import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.users.UserNotLoggedInException;
import org.piet.forumbackend.users.UserService;
import org.piet.forumbackend.users.dtos.UserDto;
import org.piet.forumbackend.users.dtos.UsersDtoMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getUserInfo(Authentication auth) throws UserNotLoggedInException {
        UserDto dto = UsersDtoMapper.toUserDto(userService.getUserFromAuth(auth));
        return ResponseEntity.ok(dto);

    }
}
