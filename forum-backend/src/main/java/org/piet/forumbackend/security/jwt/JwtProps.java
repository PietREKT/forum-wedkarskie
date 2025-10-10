package org.piet.forumbackend.security.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "forum.security.jwt")
@Data
public class JwtProps {
    private String secret;
    private String issuer;
    private Long expirySeconds;
}
