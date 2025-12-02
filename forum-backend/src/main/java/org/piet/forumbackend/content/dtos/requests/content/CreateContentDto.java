package org.piet.forumbackend.content.dtos.requests.content;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link org.piet.forumbackend.content.entities.Content}
 */
@Value
public class CreateContentDto implements Serializable {
    @NotNull
    String content;
    Long parentId;
    List<MultipartFile> photos;
}