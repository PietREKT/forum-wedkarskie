package org.piet.forumbackend.security.cookies;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "forum.security.cookies")
@Data
public class AuthCookieProps {
    private String name;
    private String domain;
    private Long expiresAfterSeconds;
}
