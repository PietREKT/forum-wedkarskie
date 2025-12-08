package org.piet.forumbackend.users.groups.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

/**
 * DTO for {@link org.piet.forumbackend.users.groups.entities.UserGroup}
 */
@Value
public class ChangeUserGroupNameDto implements Serializable {
    @NotBlank
    @Length(min = 3, max = 50)
    String name;
}