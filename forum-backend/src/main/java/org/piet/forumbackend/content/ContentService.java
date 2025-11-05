package org.piet.forumbackend.content;

import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.properties.FileProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentService {
    private final MessageSource messageSource;
    private final FileProperties fileProperties;

    private File getUserFolder(Long userId) {
        Path userFilesPath = fileProperties.getUserFilesFolder().toPath();
        Path userPath = userFilesPath.resolve("user-" + userId).normalize();
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
