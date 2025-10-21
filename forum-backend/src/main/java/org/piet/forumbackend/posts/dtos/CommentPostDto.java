package org.piet.forumbackend.posts.dtos;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link org.piet.forumbackend.posts.entities.Post}
 */
@Value
public class CommentPostDto implements Serializable {
    Long id;
}