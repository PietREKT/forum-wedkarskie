package org.piet.forumbackend.content.core.dtos.responses.tutorials;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;
import org.piet.forumbackend.content.core.entities.Tutorial;
import org.piet.forumbackend.fish.dtos.FishListDto;
import org.piet.forumbackend.fish.entities.enums.FishingMethod;
import org.piet.forumbackend.users.core.dtos.responses.ListUserDto;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link Tutorial}
 */
@Value
public class ListTutorialDto implements Serializable {
    Long id;
    @NotBlank
    String title;
    ListUserDto author;
    Set<FishingMethod> methods;
    Set<FishListDto> fishMentioned;
    Integer rating;
}