package org.piet.forumbackend.content.core.dtos.responses.content;

import lombok.Value;
import org.piet.forumbackend.content.core.entities.Content;
import org.piet.forumbackend.content.core.entities.enums.ContentType;
import org.piet.forumbackend.content.core.entities.enums.VoteType;
import org.piet.forumbackend.users.core.dtos.responses.ContentUserDto;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * DTO for {@link Content}
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