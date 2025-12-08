package org.piet.forumbackend.content.dtos.responses.tutorials;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link org.piet.forumbackend.content.entities.Tutorial}
 */
@Value
public class ListTutorialDto implements Serializable {
    Long id;
    @NotBlank
    String title;
    Integer rating;
}