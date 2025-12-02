package org.piet.forumbackend.globals.utils;

import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.globals.properties.FileProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
@RequiredArgsConstructor
public class ResourceHandlersConfig implements WebMvcConfigurer {

    private final FileProperties fileProperties;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                    .addResourceLocations("file:" + fileProperties.getUserFilesFolder().getAbsolutePath() + File.separator);
        registry.addResourceHandler("/fish/**")
                    .addResourceLocations("file:" + fileProperties.getFishFolder().getAbsolutePath() + File.separator);
        registry.addResourceHandler("/spots/**")
                .addResourceLocations("file:" + fileProperties.getSpotsFolder().getAbsolutePath() + File.separator);
    }
}
