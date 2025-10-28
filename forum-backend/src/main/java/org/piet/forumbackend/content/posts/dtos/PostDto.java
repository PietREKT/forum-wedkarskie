package org.piet.forumbackend.content.posts.dtos;

import lombok.Value;
import org.piet.forumbackend.content.posts.entities.Post;
import org.piet.forumbackend.users.dtos.PostUserDto;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * DTO for {@link Post}
 */
@Value
public class PostDto implements Serializable {
    PostUserDto author;
    String content;
    List<String> attachedPhotos;
    Map<Instant, String> editHistory;
    Long rating;
    Instant postedAt;
}