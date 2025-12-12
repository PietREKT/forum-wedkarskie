package org.piet.forumbackend.content.core.dtos.requests.tutorials;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;

import java.io.Serializable;

@Value
public class RejectTutorialDto implements Serializable {
    @NotBlank
    String rejectReason;
}
