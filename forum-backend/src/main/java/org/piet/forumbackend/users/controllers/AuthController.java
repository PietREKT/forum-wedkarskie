package org.piet.forumbackend.users.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.security.cookies.CookieBuilder;
import org.piet.forumbackend.security.jwt.JwtService;
import org.piet.forumbackend.users.UserService;
import org.piet.forumbackend.users.dtos.LoginUserDto;
import org.piet.forumbackend.users.dtos.RegisterUserDto;
import org.piet.forumbackend.users.dtos.UsersDtoMapper;
import org.piet.forumbackend.users.entities.User;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final PasswordEncoder encoder;
    private final CookieBuilder cookieBuilder;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(HttpServletRequest req, HttpServletResponse res, @RequestBody LoginUserDto dto){
        User u = userService.getUserByUsername(dto.getUsername());
        if (encoder.matches(dto.getPassword(), u.getPassword())){
            boolean secure = req.isSecure() || "https".equalsIgnoreCase(req.getHeader("X-Forwarded-Proto"));
            cookieBuilder.writeAuthCookie(res, jwtService.generate(u), secure);
            return ResponseEntity.ok(UsersDtoMapper.toUserDto(u));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Wrong credentials");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(HttpServletRequest req, HttpServletResponse res, @RequestBody RegisterUserDto dto){
        User u = userService.registerUser(dto);
        boolean secure = req.isSecure() || "https".equalsIgnoreCase(req.getHeader("X-Forwarded-Proto"));
        cookieBuilder.writeAuthCookie(res, jwtService.generate(u), secure);
        return ResponseEntity.ok(UsersDtoMapper.toUserDto(u));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest req, HttpServletResponse res){
        boolean secure = req.isSecure() || "https".equalsIgnoreCase(req.getHeader("X-Forwarded-Proto"));
        cookieBuilder.clearAuthCookie(res, secure);
        return ResponseEntity.ok().build();
    }
}
