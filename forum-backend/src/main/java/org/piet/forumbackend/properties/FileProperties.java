package org.piet.forumbackend.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@ConfigurationProperties(prefix = "forum.files.default.folders")
@Getter
@Setter
@Log4j2
public class FileProperties {
    File mainDataFolder;
    File userFilesFolder;
    File defaultUserFolder;
    File fishFolder;

    private final MessageSource messageSource;

    public FileProperties(String main, String userFiles, String defaultUser, String fish, MessageSource messageSrc) {
        Path mainPath = Paths.get(main).toAbsolutePath().normalize();
        Path userFilesPath = mainPath.resolve(userFiles).normalize();
        Path fishPath = mainPath.resolve(fish).normalize();
        Path defaultPath = userFilesPath.resolve(defaultUser).normalize();
        this.messageSource = messageSrc;

        if (!defaultPath.startsWith(userFilesPath)) {
            log.error("Default user's path must be relative to user_files folder path!");
            throw new IllegalStateException(
                    messageSource.getMessage("error.folders.path.not_relative", null, LocaleContextHolder.getLocale())
            );
        }
        if (!fishPath.startsWith(mainPath)) {
            log.error("\"fish\" folder's path must be relative to content root folder path!");
            throw new IllegalStateException(
                    messageSource.getMessage("error.folders.path.not_relative", null, LocaleContextHolder.getLocale())
            );
        }
        if (!userFilesPath.startsWith(mainPath)) {
            log.error("\"user_files\" folder's path must be relative to content root folder path!");
            throw new IllegalStateException(
                    messageSource.getMessage("error.folders.path.not_relative", null, LocaleContextHolder.getLocale())
            );
        }

        this.mainDataFolder = mainPath.toFile();
        this.userFilesFolder = userFilesPath.toFile();
        this.defaultUserFolder = defaultPath.toFile();
        this.fishFolder = fishPath.toFile();

        if (!mainDataFolder.exists() && !mainDataFolder.mkdir()) {
            log.error("Error while creating \"main_data\" directory.");
            throw new IllegalStateException(
                    messageSource.getMessage("error.folders.creation.default.main",
                            null, LocaleContextHolder.getLocale()
                    ));
        }
        if (!userFilesFolder.exists() && !userFilesFolder.mkdir()) {
            log.error("Error while creating \"user_files\" directory.");
            throw new IllegalStateException(
                    messageSource.getMessage("error.folders.creation.default.user_files",
                            null, LocaleContextHolder.getLocale()
                    ));
        }
        if (!defaultUserFolder.exists() && !defaultUserFolder.mkdir()) {
            log.error("Error while creating \"default_user\" directory.");
            throw new IllegalStateException(
                    messageSource.getMessage("error.folders.creation.default.user",
                            null, LocaleContextHolder.getLocale()
                    ));
        }

        if (!fishFolder.exists() && !fishFolder.mkdir()) {
            log.error("Error while creating \"fish\" directory.");
            throw new IllegalStateException(
                    messageSource.getMessage("error.folders.creation.default.user",
                            null, LocaleContextHolder.getLocale()
                    ));
        }
    }
}
