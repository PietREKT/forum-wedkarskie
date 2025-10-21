package org.piet.forumbackend.posts.dtos;

import lombok.Value;
import org.piet.forumbackend.users.dtos.PostUserDto;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;

/**
 * DTO for {@link org.piet.forumbackend.posts.entities.Post}
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