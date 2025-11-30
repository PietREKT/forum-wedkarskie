package org.piet.forumbackend;

import org.piet.forumbackend.globals.properties.FileProperties;
import org.piet.forumbackend.globals.properties.JwtProperties;
import org.piet.forumbackend.globals.properties.PaginationProperties;
import org.piet.forumbackend.globals.security.cookies.AuthCookieProps;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, AuthCookieProps.class, PaginationProperties.class, FileProperties.class})
public class ForumBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForumBackendApplication.class, args);
    }

}
