package org.piet.forumbackend.globals.security.cookies;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class CookieBuilder {
    private final AuthCookieProps authCookieProps;

    public void writeAuthCookie(HttpServletResponse resp, String jwt, boolean secure){
        ResponseCookie cookie = ResponseCookie.from(authCookieProps.getName(), jwt)
                .httpOnly(true)
                .secure(secure)
//                .domain(authCookieProps.getDomain())
                .maxAge(authCookieProps.getExpiresAfterSeconds())
                .sameSite("Lax")
                .path("/")
                .build();
        resp.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void clearAuthCookie(HttpServletResponse response, boolean secure){
        ResponseCookie cookie = ResponseCookie.from(authCookieProps.getName(), "")
                .httpOnly(true)
                .secure(secure)
//                .domain(authCookieProps.getDomain())
                .maxAge(Duration.ZERO)
                .sameSite("Lax")
                .path("/")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
