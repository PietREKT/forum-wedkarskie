package org.piet.forumbackend.globals.properties.file_properties;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
    File spotsFolder;
    File contentFolder;
    File statuesFolder;
    File spotsPicsFolder;

    final String contentFolderUploadsUrl;

    private final MessageSource messageSource;

    public FileProperties(String main, String userFiles, String defaultUser, String fish, String spots, String content, MessageSource messageSrc) {
        Path mainPath = Paths.get(main).toAbsolutePath().normalize();
        Path userFilesPath = mainPath.resolve(userFiles).normalize();
        Path fishPath = mainPath.resolve(fish).normalize();
        Path defaultPath = userFilesPath.resolve(defaultUser).normalize();
        Path spotsPath = mainPath.resolve(spots).normalize();
        Path contentPath = userFilesPath.resolve(content).normalize();
        this.messageSource = messageSrc;

        if (!defaultPath.startsWith(userFilesPath)) {
            log.error("Default user's path must be relative to user_files folder path!");
            throw new IllegalStateException(
                    messageSource.getMessage("error.folders.path.not_relative", null, LocaleContextHolder.getLocale())
            );
        }
        if (!contentPath.startsWith(userFilesPath)) {
            log.error("\"content\" folder's path must be relative to user_files folder path!");
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
        this.spotsFolder = spotsPath.toFile();
        this.contentFolder = contentPath.toFile();
        statuesFolder = new File(spotsFolder, "statues");
        spotsPicsFolder = new File(spotsFolder, "pics");

        this.contentFolderUploadsUrl = "/uploads/" + contentFolder.getName() + '/';
    }

    private void ensureDirectoryExists(File dir) {
        try {
            Files.createDirectories(dir.toPath());
        } catch (IOException e) {
            throw new IllegalStateException("Error while creating directory: " + dir.getAbsolutePath(), e);
        }
    }


    public static String getFileExtension(String filename){
        if (filename == null) throw new IllegalArgumentException("Filename can not be null!");
        if (!filename.contains(".")) {
            log.error("Filename: {} without extension provided for file extension getter.", filename);
            throw new IllegalArgumentException("Filename without extension provided");
        }
        return filename.substring(filename.lastIndexOf('.'));
    }
}
