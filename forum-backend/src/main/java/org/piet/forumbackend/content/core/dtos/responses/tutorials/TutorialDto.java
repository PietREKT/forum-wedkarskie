package org.piet.forumbackend.content.core.dtos.responses.tutorials;

import lombok.Value;
import org.piet.forumbackend.content.core.dtos.responses.content.ContentDto;
import org.piet.forumbackend.content.core.entities.Tutorial;
import org.piet.forumbackend.fish.dtos.FishListDto;
import org.piet.forumbackend.fish.entities.enums.FishingMethod;
import org.piet.forumbackend.users.core.dtos.responses.UserDto;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link Tutorial}
 */
@Value
public class TutorialDto implements Serializable {
    UserDto verifiedBy;
    Set<FishingMethod> methods;
    Set<FishListDto> fishMentioned;
    ContentDto content;
}