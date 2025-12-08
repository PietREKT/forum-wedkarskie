package org.piet.forumbackend.users.groups.dtos.responses;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link org.piet.forumbackend.users.groups.entities.UserGroup}
 */
@Value
public class ListUserGroupDto implements Serializable {
    UUID id;
    @NotBlank
    @Length(min = 3, max = 50)
    String name;
    Integer memberCount;
}