package org.piet.forumbackend.content;

import org.piet.forumbackend.content.posts.repostitories.PostRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
public class ContentService {
    private final MessageSource messageSource;


    File mainFolder;

    File defaultFolder;

    public ContentService(
            PostRepository postRepository,
            @Value("${forum.files.main_folder}") String mainFolderName,
            @Value("${forum.files.defaultFolder}") String defaultFolderName, MessageSource messageSource
    ) {
        this.messageSource = messageSource;

        Path mainPath = Paths.get(mainFolderName).toAbsolutePath().normalize();
        Path defaultPath = mainPath.resolve(defaultFolderName).normalize();

        if (!defaultPath.startsWith(mainPath)) {
            throw new IllegalStateException(
                    messageSource.getMessage("error.folders.path.not_relative", null, LocaleContextHolder.getLocale())
            );
        }

        this.mainFolder = mainPath.toFile();
        this.defaultFolder = defaultPath.toFile();

        if (!mainFolder.exists() && !mainFolder.mkdir()) {
            throw new IllegalStateException(
                    messageSource.getMessage("error.folders.creation.default.main",
                            null, LocaleContextHolder.getLocale()
            ));
        }
        if (!defaultFolder.exists() && !defaultFolder.mkdir()) {
            throw new IllegalStateException(
                    messageSource.getMessage("error.folders.creation.default.user",
                            null, LocaleContextHolder.getLocale()
            ));
        }
    }

    private File getUserFolder(Long userId) {
        Path mainPath = mainFolder.toPath();
        Path userPath = mainPath.resolve("user-" + userId).normalize();
        File userFolder = userPath.toFile();

        if (!userFolder.exists() && !userFolder.mkdir()) {
            throw new IllegalStateException(
                    messageSource.getMessage(
                            "error.folders.creation.user",
                            new Object[]{userId},
                            LocaleContextHolder.getLocale()
                    )
            );
        }

        return userFolder;
    }

    private File getUserContentFolder(Long authorId, Long id, String prefix){
        File userFolder = getUserFolder(authorId);
        return getUserContentFolder(userFolder, id, prefix);
    }

    private File getUserContentFolder(File userFolder, Long id, String prefix){
        Path userFolderPath = userFolder.toPath().normalize();
        File contentFolder = userFolderPath.resolve( prefix+ "-" + id).normalize().toFile();

        if (!contentFolder.exists() && !contentFolder.mkdir()) {
            throw new IllegalStateException(
                    messageSource.getMessage(
                            "error.folders.creation.post",
                            new Object[]{id},
                            LocaleContextHolder.getLocale()
                    )
            );
        }

        return contentFolder;
    }

    public File getPostFolder(File userFolder, Long postId) {
        return getUserContentFolder(userFolder, postId, "post");
    }

    public File getCommentFolder(File userFolder, Long commentId){
        return getUserContentFolder(userFolder, commentId, "comment");
    }

    private List<String> saveToUserContentFolder(Long authorId, Long id, String prefix, List<MultipartFile> toSave) throws IOException {
        File contentFolder = getUserContentFolder(authorId, id, prefix);
        List<String> paths = new ArrayList<>();
        if (toSave == null)
            return paths;

        for (MultipartFile file : toSave){
            String ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'));
            File f = new File(contentFolder, prefix + "-" + id + "__" + toSave.indexOf(file) + ext);
            file.transferTo(f);
            paths.add("/uploads/" + contentFolder.getParentFile().getName() + '/' + contentFolder.getName() + '/' + f.getName());
        }

        return paths;
    }

    public List<String> saveToPostFolder(Long authorId, Long postId, List<MultipartFile> toSave) throws IOException {
        return saveToUserContentFolder(authorId, postId, "post", toSave);
    }

    public String saveToCommentsFolder(Long authorId, Long commentId, MultipartFile toSave) throws IOException {
        var paths = saveToUserContentFolder(authorId, commentId, "comment", List.of(toSave));
        return paths.getFirst();
    }

    public boolean deletePostFolder(Long userId, Long postId){
        File userFolder = getUserFolder(userId);
        File postFolder = getPostFolder(userFolder, postId);
        return postFolder.delete();
    }

    public boolean deleteCommentFolder(Long userId, Long commentId){
        File userFolder = getUserFolder(userId);
        File commentFolder = getCommentFolder(userFolder, commentId);
        return commentFolder.delete();
    }
}
