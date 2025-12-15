package org.piet.forumbackend.globals.properties.file_properties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class FilePropertiesFolderInitializer implements ApplicationRunner {
    private final FileProperties fileProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Arrays.stream(FileProperties.class.getDeclaredFields())
                .filter(f -> f.getType() == File.class)
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                .forEach(this::ensureFieldDir);
    }

    private void ensureFieldDir(Field f) {
        try {
            f.setAccessible(true);
            File dir = (File) f.get(fileProperties);
            if (dir == null) return;

            Path p = dir.toPath();
            Files.createDirectories(p);

            if (!Files.isDirectory(p)) {
                throw new IllegalStateException("Path exists but is not a directory: " + p.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create directory for field: " + f.getName(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot access field: " + f.getName(), e);
        }
    }
}
