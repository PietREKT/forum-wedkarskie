package org.piet.forumbackend.fishing_spots.core.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Value
public class UploadSpotFileDto implements Serializable {
    @NotNull
    MultipartFile file;
}
