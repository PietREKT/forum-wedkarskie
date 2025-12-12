package org.piet.forumbackend.users.core.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Value
public class ChangeProfilePicDto implements Serializable {
    @NotNull
    MultipartFile profilePic;
}
