package org.piet.forumbackend.content.core.dtos.responses.tutorials;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;
import org.piet.forumbackend.content.core.entities.Tutorial;

import java.io.Serializable;

/**
 * DTO for {@link Tutorial}
 */
@Value
public class ListTutorialDto implements Serializable {
    Long id;
    @NotBlank
    String title;
    Integer rating;
}