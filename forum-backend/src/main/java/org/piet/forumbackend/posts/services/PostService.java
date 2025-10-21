package org.piet.forumbackend.posts.services;

import org.piet.forumbackend.posts.entities.Post;
import org.piet.forumbackend.posts.exceptions.PostNotFoundException;
import org.piet.forumbackend.posts.repostitories.PostRepository;
import org.piet.forumbackend.users.entities.Role;
import org.piet.forumbackend.users.entities.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Service
public class PostService {

    private final PostRepository postRepository;

    File mainFolder;

    File defaultFolder;

    public PostService(
            PostRepository postRepository,
            @Value("${forum.files.main_folder}") String mainFolderName,
            @Value("${forum.files.defaultFolder}") String defaultFolderName) {
        this.postRepository = postRepository;

        Path mainPath = Paths.get(mainFolderName).toAbsolutePath().normalize();
        Path defaultPath = mainPath.resolve(defaultFolderName).normalize();

        if (!defaultPath.startsWith(mainPath)) {
            throw new IllegalStateException("Default path must be relative to main path!");
        }

        this.mainFolder = mainPath.toFile();
        this.defaultFolder = defaultPath.toFile();

        if (!mainFolder.exists() && !mainFolder.mkdir()) {
            throw new IllegalStateException("Error while creating 'main user folder' directory");
        }
        if (!defaultFolder.exists() && !defaultFolder.mkdir()) {
            throw new IllegalStateException("Error while creating 'default user folder' directory");
        }
    }

    private File getUserFolder(Long userId) {
        Path mainPath = mainFolder.toPath();
        Path userPath = mainPath.resolve("user-" + userId).normalize();
        File userFolder = userPath.toFile();

        if (!userFolder.exists() && !userFolder.mkdir()) {
            throw new IllegalStateException("Error while creating directory for userId: " + userId);
        }

        return userFolder;
    }

    private File getPostFolder(File userFolder, Long postId) {
        Path userFolderPath = userFolder.toPath().normalize();
        File postFolder = userFolderPath.resolve("post-" + postId).normalize().toFile();

        if (!postFolder.exists() && !postFolder.mkdir()) {
            throw new IllegalStateException("Error while creating directory for postId: " + postId);
        }

        return postFolder;
    }

    public Post getPostById(Long postId) throws PostNotFoundException {
        return postRepository.findById(postId).orElseThrow(() -> new PostNotFoundException("Post with id: " + postId + " not found."));
    }

    public Post createPost(User author, String content, List<MultipartFile> attachments) throws IOException {
        File userFolder = getUserFolder(author.getId());
        Post post = new Post();

        post.setAuthor(author);
        post.setContent(content);
        post = postRepository.save(post);
        File postFolder = getPostFolder(userFolder,post.getId());
        if (attachments == null) {
            attachments = new ArrayList<>();
        }
        List<String> paths = new ArrayList<>();
        for (MultipartFile file : attachments) {
            String ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'));
            File f = new File(postFolder, "post-" + post.getId() + "__" + attachments.indexOf(file) + ext);
            file.transferTo(f);
            paths.add("/uploads/" + userFolder.getName() + '/' + postFolder.getName() + '/' + f.getName());
        }
        post.setAttachedPhotos(paths);
        return postRepository.save(post);
    }

    public void deletePost(Long postId, User user) throws PostNotFoundException {
        Post p = getPostById(postId);
        //TODO Make sure admins can delete posts
        if (!Objects.equals(p.getAuthor().getId(), user.getId()) &&
                user.getRoles().stream().map(Role::getPermLevel).noneMatch(perm -> perm >= 3)){
            throw new IllegalAccessError("You don't have permissions to delete other users' posts");
        }
        postRepository.delete(p);
    }

    public Post editPostContent(User currentUser, Long postId, String newContent) throws PostNotFoundException {
        Post p = getPostById(postId);

        if (!p.getAuthor().equals(currentUser)){
            throw new IllegalAccessError("You can't edit other users' posts.");
        }

        HashMap<Instant, String> editHistory = p.getEditHistory();
        editHistory.put(Instant.now(), p.getContent());
        p.setContent(newContent);
        p.setEditHistory(editHistory);
        return postRepository.save(p);
    }

    private void updateRating(Post p, Long rating){
        p.setRating(rating);
        postRepository.save(p);
    }

    public void upvote(Long postId) throws PostNotFoundException {
        Post p = getPostById(postId);
        updateRating(p, p.getRating() + 1);
    }

    public void downvote(Long postId) throws PostNotFoundException {
        Post p = getPostById(postId);
        updateRating(p, p.getRating() - 1);
    }
}
