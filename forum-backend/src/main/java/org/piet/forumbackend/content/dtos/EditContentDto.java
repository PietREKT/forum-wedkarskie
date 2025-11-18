package org.piet.forumbackend.content.dtos;

import lombok.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link ContentDto}
 */
@Value
public class EditContentDto implements Serializable {
    Long id;
    String content;
    List<String> attachedPhotos;
    List<MultipartFile> newPhotos;
}