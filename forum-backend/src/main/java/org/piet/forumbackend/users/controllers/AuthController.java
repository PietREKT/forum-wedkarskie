package org.piet.forumbackend.users.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.security.cookies.CookieBuilder;
import org.piet.forumbackend.security.jwt.JwtService;
import org.piet.forumbackend.users.UserService;
import org.piet.forumbackend.users.dtos.LoginUserDto;
import org.piet.forumbackend.users.dtos.RegisterUserDto;
import org.piet.forumbackend.users.dtos.UserDto;
import org.piet.forumbackend.users.dtos.UsersDtoMapper;
import org.piet.forumbackend.users.entities.User;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${forum.api.prefix}/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Endpoints for user authentication")
public class AuthController {
    private final UserService userService;
    private final PasswordEncoder encoder;
    private final CookieBuilder cookieBuilder;
    private final JwtService jwtService;
    private final MessageSource messageSource;


    @PostMapping("/login")
    public ResponseEntity<UserDto> login(HttpServletRequest req, HttpServletResponse res, @RequestBody LoginUserDto dto) {
        User u = userService.getUserByUsername(dto.getUsername());
        if (encoder.matches(dto.getPassword(), u.getPassword())) {
            boolean secure = req.isSecure() || "https".equalsIgnoreCase(req.getHeader("X-Forwarded-Proto"));
            cookieBuilder.writeAuthCookie(res, jwtService.generate(u), secure);
            return ResponseEntity.ok(UsersDtoMapper.toUserDto(u));
        }
        throw new BadCredentialsException(
                messageSource.getMessage("error.users.bad_credentials", null, LocaleContextHolder.getLocale())
        );
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(HttpServletRequest req, HttpServletResponse res, @RequestBody RegisterUserDto dto) {
        User u = userService.registerUser(dto);
        boolean secure = req.isSecure() || "https".equalsIgnoreCase(req.getHeader("X-Forwarded-Proto"));
        cookieBuilder.writeAuthCookie(res, jwtService.generate(u), secure);
        return ResponseEntity.ok(UsersDtoMapper.toUserDto(u));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest req, HttpServletResponse res) {
        boolean secure = req.isSecure() || "https".equalsIgnoreCase(req.getHeader("X-Forwarded-Proto"));
        cookieBuilder.clearAuthCookie(res, secure);
        return ResponseEntity.ok(
                messageSource.getMessage("users.logged_out",
                        null, LocaleContextHolder.getLocale())
        );
    }
}
