package org.piet.forumbackend.posts.dtos;

import lombok.Value;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for {@link org.piet.forumbackend.posts.entities.Comment}
 */
@Value
public class CommentDto implements Serializable {
    Long id;
    String content;
    Instant createdAt;
}