package org.piet.forumbackend.content.dtos;

import lombok.Value;
import org.piet.forumbackend.content.entities.enums.ContentType;
import org.piet.forumbackend.content.entities.enums.VoteType;
import org.piet.forumbackend.users.core.dtos.ContentUserDto;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * DTO for {@link org.piet.forumbackend.content.entities.Content}
 */
@Value
public class ContentDto implements Serializable {
    Long id;
    ContentUserDto author;
    String content;
    Instant createdAt;
    ContentType contentType;
    ParentContentDto parent;
    List<String> attachedPhotos;
    Map<Instant, String> editHistory;
    Integer rating;
    VoteType loggedUserVote;
}