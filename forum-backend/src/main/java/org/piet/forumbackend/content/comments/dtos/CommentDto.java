package org.piet.forumbackend.content.comments.dtos;

import lombok.Value;
import org.piet.forumbackend.content.comments.Comment;
import org.piet.forumbackend.users.dtos.PostUserDto;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for {@link Comment}
 */
@Value
public class CommentDto implements Serializable {
    Long id;
    String content;
    Instant createdAt;
    Long postId;
    Long parentId;
    PostUserDto author;
}