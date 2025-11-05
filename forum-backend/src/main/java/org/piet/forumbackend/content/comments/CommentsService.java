package org.piet.forumbackend.content.comments;

import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.content.ContentBaseServiceInt;
import org.piet.forumbackend.content.ContentService;
import org.piet.forumbackend.content.posts.entities.Post;
import org.piet.forumbackend.exceptions.NotFoundException;
import org.piet.forumbackend.users.entities.Role;
import org.piet.forumbackend.users.entities.User;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommentsService implements ContentBaseServiceInt<Comment> {
    private final CommentRepository commentRepository;
    private final MessageSource messageSource;
    private final int MOD_PERM_LEVEL = 3;
    private final ContentService contentService;

    @Override
    public Comment getContentById(Long commentId) throws NotFoundException {
        return commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException(
                messageSource.getMessage("errors.comments.not_found",
                        new Object[]{commentId},
                        LocaleContextHolder.getLocale()
                )
        ));
    }

    public List<Comment> getCommentsByPostId(Long postId, Pageable pageable) {
        return commentRepository.findByPost_Id(postId, pageable);
    }

    public Comment createComment(Post post, String content, User author, MultipartFile attachment) throws IOException {
        Comment comment = new Comment();
        comment.setPost(post);
        comment.setContent(content);
        comment.setAuthor(author);
        comment = commentRepository.save(comment);
        if (attachment != null){
            comment.setAttachmentUrl(contentService.saveToCommentsFolder(author.getId(), comment.getId(), attachment));
        }
        return commentRepository.save(comment);
    }

    @Override
    public Comment editContent(User currentUser, Long commentId, String newContent) throws NotFoundException {
        Comment comment = getContentById(commentId);

        if (!comment.getAuthor().equals(currentUser)){
            throw new IllegalAccessError(
                    messageSource.getMessage("error.comments.no_perms_for_edition", null, LocaleContextHolder.getLocale())
            );
        }

        Map<Instant, String> editHistory = comment.getEditHistory();
        editHistory.put(Instant.now(), newContent);
        comment.setEditHistory(editHistory);
        comment.setContent(newContent);
        return commentRepository.save(comment);
    }

    private void updateRating(Comment c, Long rating) {
        c.setRating(rating);
        commentRepository.save(c);
    }

    @Override
    public void upvote(Long contentId) throws Exception {
        Comment c = getContentById(contentId);
        updateRating(c, c.getRating() + 1);
    }

    @Override
    public void downvote(Long contentId) throws Exception {
        Comment c = getContentById(contentId);
        updateRating(c, c.getRating() - 1);
    }

    @Override
    public void deleteContent(Long commentId, User u) throws NotFoundException{
        Comment c = getContentById(commentId);
        if (!c.getAuthor().equals(u) && u.hasPermLevelAtLeast(Role.MOD)){
            throw new IllegalAccessError(
                    messageSource.getMessage("error.comments.no_perms_for_deletion",
                            null, LocaleContextHolder.getLocale()));
        }
        commentRepository.delete(c);
    }
}
