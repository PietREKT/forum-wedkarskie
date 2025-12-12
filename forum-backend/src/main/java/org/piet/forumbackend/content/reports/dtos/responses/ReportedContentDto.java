package org.piet.forumbackend.content.reports.dtos.responses;

import lombok.Value;
import org.piet.forumbackend.content.core.entities.Content;
import org.piet.forumbackend.content.core.entities.enums.ContentType;
import org.piet.forumbackend.users.core.dtos.responses.ContentUserDto;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * DTO for {@link Content}
 */
@Value
public class ReportedContentDto implements Serializable {
    Long id;
    ContentUserDto author;
    String content;
    Instant createdAt;
    ContentType contentType;
    List<String> attachedPhotos;

    public static ReportedContentDto create(Content content){
        return new ReportedContentDto(
                content.getId(),
            ContentUserDto.create(content.getAuthor()),
                content.getContent(),
                content.getCreatedAt(),
                content.getContentType(),
                content.getAttachedPhotos()
        );
    }
}