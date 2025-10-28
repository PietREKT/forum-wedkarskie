package org.piet.forumbackend.content.comments.dtos;

import lombok.Value;
import org.piet.forumbackend.content.posts.entities.Post;

import java.io.Serializable;

/**
 * DTO for {@link Post}
 */
@Value
public class CommentPostDto implements Serializable {
    Long id;
}