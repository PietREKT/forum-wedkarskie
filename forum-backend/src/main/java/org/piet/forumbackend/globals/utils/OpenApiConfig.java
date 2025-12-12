package org.piet.forumbackend.globals.utils;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public GroupedOpenApi forumApi(){
        return GroupedOpenApi.builder()
                .group("default")
                .pathsToMatch("/api/**")
                .pathsToExclude("/api/admin/**", "/api/users/**", "/api/mod/**")
                .displayName("Default")
                .build();
    }

    @Bean
    GroupedOpenApi usersApi(){
        return GroupedOpenApi.builder()
                .group("users")
                .pathsToMatch("/api/users/**")
                .pathsToExclude("/api/users/groups/**")
                .displayName("Users")
                .build();
    }

    @Bean
    public GroupedOpenApi groupsApi(){
        return GroupedOpenApi.builder()
                .group("groups")
                .pathsToMatch("/api/users/groups/**")
                .displayName("User groups")
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi(){
        return GroupedOpenApi.builder()
                .group("admin")
                .pathsToMatch("/api/admin/**", "/api/mod/**")
                .displayName("Admins and mods")
                .build();
    }
}
