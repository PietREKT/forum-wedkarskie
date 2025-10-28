package org.piet.forumbackend.content.posts.dtos;

import lombok.Value;
import org.piet.forumbackend.content.comments.dtos.CommentDto;
import org.piet.forumbackend.content.posts.entities.Post;
import org.piet.forumbackend.users.dtos.PostUserDto;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;

/**
 * DTO for {@link Post}
 */
@Value
public class PostWithCommentsDto implements Serializable {
    Long id;
    PostUserDto author;
    String content;
    List<String> attachedPhotos;
    HashMap editHistory;
    Long rating;
    Instant postedAt;
    List<CommentDto> comments;
}