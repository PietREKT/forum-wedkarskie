package org.piet.forumbackend.globals.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.globals.properties.JwtProperties;
import org.piet.forumbackend.users.core.entities.User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties jwtProperties;

    private SecretKey getKey(){
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generate(User user){
        Instant instant = Instant.now();
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole().name());

        return Jwts.builder()
                .subject(user.getId().toString())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(instant))
                .expiration(Date.from(instant.plusSeconds(jwtProperties.getExpirySeconds())))
                .claims(claims)
                .signWith(getKey())
                .compact();
    }

    public Jws<Claims> parse(String token){
        return Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token);
    }
}
