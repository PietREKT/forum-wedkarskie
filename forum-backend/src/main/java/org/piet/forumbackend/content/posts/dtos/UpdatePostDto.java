package org.piet.forumbackend.content.posts.dtos;

import lombok.Value;
import org.piet.forumbackend.content.posts.entities.Post;

import java.io.Serializable;

/**
 * DTO for {@link Post}
 */
@Value
public class UpdatePostDto implements Serializable {
    Long id;
    String content;
}