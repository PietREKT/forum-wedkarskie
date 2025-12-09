package org.piet.forumbackend.globals.security.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.piet.forumbackend.globals.security.SecurityUserDto;
import org.piet.forumbackend.globals.security.cookies.AuthCookieProps;
import org.piet.forumbackend.globals.security.cookies.CookieBuilder;
import org.piet.forumbackend.users.core.repos.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

import static org.piet.forumbackend.globals.utils.UserMessagesDateTimeFormatter.BAN_TIME_FORMATTER;

@Component
@RequiredArgsConstructor
@Log4j2
public class JwtFilter extends OncePerRequestFilter {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthCookieProps authCookieProps;
    private final CookieBuilder cookieBuilder;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromCookie(request, authCookieProps.getName());

        if (token != null){
            Claims claims = jwtService.parse(token).getPayload();
            UUID uid = UUID.fromString(claims.getSubject());

            userRepository.findById(uid).ifPresentOrElse(u -> {
                if (u.isBanned(Instant.now())) {
                    cookieBuilder.clearAuthCookie(response, false);
                    throw new AccessDeniedException("You are banned until: " + BAN_TIME_FORMATTER.format(u.getBannedUntil()));
                }
                SecurityUserDto su = new SecurityUserDto(u.getId(), u.getUsername());
                var auth = new UsernamePasswordAuthenticationToken(su, null, u.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }, () -> {log.warn("User with id: {} in token not found!", uid);});
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromCookie(HttpServletRequest req, String cookieName){
        if (req.getCookies() == null) return null;

        for (var cookie : req.getCookies()){
            if (cookie.getName().equals(cookieName)){
                return cookie.getValue();
            }
        }
        return null;
    }
}
