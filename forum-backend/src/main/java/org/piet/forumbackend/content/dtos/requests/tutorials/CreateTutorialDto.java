package org.piet.forumbackend.content.dtos.requests.tutorials;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;

@Value
public class CreateTutorialDto implements Serializable {
    @NotBlank
    String title;
    @NotBlank
    String content;
    List<MultipartFile> photos;
}
