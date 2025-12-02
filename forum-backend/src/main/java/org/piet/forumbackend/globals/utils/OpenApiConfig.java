package org.piet.forumbackend.globals.utils;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI forumAPi(){
        return new OpenAPI()
                .info(new Info()
                        .title("Forum API")
                        .description("REST API Docs for Forum Wedkarskie")
                        .version("1.0.0")
                );
    }
}
