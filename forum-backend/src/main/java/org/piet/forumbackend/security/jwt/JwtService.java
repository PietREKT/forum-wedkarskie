package org.piet.forumbackend.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.users.entities.User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtService {
    private final JwtProps jwtProps;

    private SecretKey getKey(){
        return Keys.hmacShaKeyFor(jwtProps.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generate(User user){
        Instant instant = Instant.now();
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("roles", user.getRoles());

        return Jwts.builder()
                .subject(user.getId().toString())
                .issuer(jwtProps.getIssuer())
                .issuedAt(Date.from(instant))
                .expiration(Date.from(instant.plusSeconds(jwtProps.getExpirySeconds())))
                .claims(claims)
                .signWith(getKey())
                .compact();
    }

    public Jws<Claims> parse(String token){
        return Jwts.parser().decryptWith(getKey()).build().parseSignedClaims(token);
    }
}
