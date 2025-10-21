package org.piet.forumbackend.posts.dtos;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link org.piet.forumbackend.posts.entities.Comment}
 */
@Value
public class CreateCommentDto implements Serializable {
    String content;
    CommentPostDto post;
}