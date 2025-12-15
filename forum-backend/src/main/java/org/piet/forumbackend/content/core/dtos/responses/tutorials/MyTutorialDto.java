package org.piet.forumbackend.content.core.dtos.responses.tutorials;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;
import org.piet.forumbackend.content.core.entities.enums.VerificationStatus;
import org.piet.forumbackend.fish.dtos.FishListDto;
import org.piet.forumbackend.fish.entities.enums.FishingMethod;
import org.piet.forumbackend.users.core.dtos.responses.ListUserDto;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * DTO for {@link org.piet.forumbackend.content.core.entities.Tutorial}
 */
@Value
public class MyTutorialDto implements Serializable {
    Long id;
    ListUserDto author;
    String content;
    Instant createdAt;
    List<String> attachedPhotos;
    @NotBlank
    String title;
    VerificationStatus verificationStatus;
    ListUserDto verifiedBy;
    String rejectionReason;
    Set<FishingMethod> methods;
    Set<FishListDto> fishMentioned;
}