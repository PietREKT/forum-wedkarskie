package org.piet.forumbackend.content.comments.dtos;

import lombok.Value;
import org.piet.forumbackend.content.comments.Comment;

import java.io.Serializable;

/**
 * DTO for {@link Comment}
 */
@Value
public class CreateCommentDto implements Serializable {
    String content;
    CommentPostDto post;
}