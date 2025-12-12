package org.piet.forumbackend.content.core.dtos.requests.content;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.piet.forumbackend.content.core.entities.Content;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * DTO for {@link Content}
 */
@Value
public class CreateContentDto implements Serializable {
    @NotNull
    String content;
    Long parentId;
    List<MultipartFile> photos;
    UUID groupId;
}