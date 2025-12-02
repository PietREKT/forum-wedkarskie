package org.piet.forumbackend.users.groups.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link org.piet.forumbackend.users.groups.entities.UserGroup}
 */
@Value
public class ChangeUserGroupNameDto implements Serializable {
    @NotNull
    UUID id;
    @NotBlank
    @Length(min = 3, max = 50)
    String name;
}