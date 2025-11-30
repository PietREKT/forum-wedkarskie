package org.piet.forumbackend.globals.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "forum.security.jwt")
@Data
public class JwtProperties {
    private String secret;
    private String issuer;
    private Long expirySeconds;
}
