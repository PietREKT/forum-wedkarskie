package org.piet.forumbackend;

import org.piet.forumbackend.security.cookies.AuthCookieProps;
import org.piet.forumbackend.security.jwt.JwtProps;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProps.class, AuthCookieProps.class})
public class ForumBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForumBackendApplication.class, args);
    }

}
