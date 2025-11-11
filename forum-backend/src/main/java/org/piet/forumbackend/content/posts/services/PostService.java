package org.piet.forumbackend.content.posts.services;

import org.piet.forumbackend.content.ContentBaseServiceInt;
import org.piet.forumbackend.content.ContentService;
import org.piet.forumbackend.content.posts.entities.Post;
import org.piet.forumbackend.content.posts.repostitories.PostRepository;
import org.piet.forumbackend.exceptions.NotFoundException;
import org.piet.forumbackend.users.entities.Role;
import org.piet.forumbackend.users.entities.User;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class PostService implements ContentBaseServiceInt<Post> {

    private final PostRepository postRepository;
    private final MessageSource messageSource;
    private final ContentService contentService;

    public PostService(PostRepository postRepository, MessageSource messageSource, ContentService contentService) {
        this.postRepository = postRepository;
        this.messageSource = messageSource;
        this.contentService = contentService;
    }

    @Override
    public Post getContentById(Long postId) throws NotFoundException {
        return postRepository.findById(postId).orElseThrow(() -> new NotFoundException(
                messageSource.getMessage("error.posts.not_found",
                        new Object[]{postId},
                        LocaleContextHolder.getLocale()
                )
        ));
    }

    public Post createPost(User author, String content, List<MultipartFile> attachments) throws IOException {
        Post post = new Post();

        post.setAuthor(author);
        post.setContent(content);
        post = postRepository.save(post);
        var paths = contentService.saveToPostFolder(author.getId(), post.getId(), attachments);
        post.setAttachedPhotos(paths);
        return postRepository.save(post);
    }

    @Override
    public void deleteContent(Long postId, User user) throws NotFoundException {
        Post p = getContentById(postId);
        //TODO Make sure admins can delete posts
        if (!Objects.equals(p.getAuthor().getId(), user.getId()) && !user.hasPermLevelAtLeast(Role.MOD)) {
            throw new IllegalAccessError(
                    messageSource.getMessage("error.posts.no_perms_for_deletion", null, LocaleContextHolder.getLocale())
            );
        }
        contentService.deletePostFolder(user.getId(), postId);
        postRepository.delete(p);
    }

    @Override
    public Post editContent(User currentUser, Long postId, String newContent) throws NotFoundException {
        Post p = getContentById(postId);

        if (!p.getAuthor().equals(currentUser)) {
            throw new IllegalAccessError(
                    messageSource.getMessage("error.posts.no_perms_for_edition", null, LocaleContextHolder.getLocale())
            );
        }

        Map<Instant, String> editHistory = p.getEditHistory();
        editHistory.put(Instant.now(), newContent);
        p.setContent(newContent);
        p.setEditHistory(editHistory);
        return postRepository.save(p);
    }

    public Page<Post> getRecentPosts(int pageNo, int pageSize){
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepository.findAll(pageable);
    }

    private void updateRating(Post p, Long rating) {
        p.setRating(rating);
        postRepository.save(p);
    }

    @Override
    public void upvote(Long postId) throws NotFoundException {
        Post p = getContentById(postId);
        updateRating(p, p.getRating() + 1);
    }

    @Override
    public void downvote(Long postId) throws NotFoundException {
        Post p = getContentById(postId);
        updateRating(p, p.getRating() - 1);
    }
}
