package org.piet.forumbackend.security.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.piet.forumbackend.security.SecurityUserDto;
import org.piet.forumbackend.security.cookies.AuthCookieProps;
import org.piet.forumbackend.users.entities.Role;
import org.piet.forumbackend.users.repos.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Log4j2
public class JwtFilter extends OncePerRequestFilter {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthCookieProps authCookieProps;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromCookie(request, authCookieProps.getName());

        if (token != null){
            Claims claims = jwtService.parse(token).getPayload();
            Role role = Role.valueOf(claims.get("role", String.class));
            Long uid = Long.valueOf(claims.getSubject());

            userRepository.findById(uid).ifPresent(u -> {
                SecurityUserDto su = new SecurityUserDto(u.getId(), u.getUsername());
                var auth = new UsernamePasswordAuthenticationToken(su, null, List.of(role.getAsAuthority()));
                SecurityContextHolder.getContext().setAuthentication(auth);
            });
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
