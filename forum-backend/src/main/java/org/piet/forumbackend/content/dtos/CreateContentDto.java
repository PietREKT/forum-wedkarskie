package org.piet.forumbackend.content.dtos;

import lombok.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link org.piet.forumbackend.content.entities.Content}
 */
@Value
public class CreateContentDto implements Serializable {
    String content;
    Long parentId;
    List<MultipartFile> photos;
}