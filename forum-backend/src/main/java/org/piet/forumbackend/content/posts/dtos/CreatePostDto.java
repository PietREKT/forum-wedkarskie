package org.piet.forumbackend.content.posts.dtos;

import lombok.Value;
import org.piet.forumbackend.content.posts.entities.Post;

import java.io.Serializable;

/**
 * DTO for {@link Post}
 */
@Value
public class CreatePostDto implements Serializable {
    String content;
}