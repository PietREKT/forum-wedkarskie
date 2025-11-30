package org.piet.forumbackend.users.groups.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.hibernate.validator.constraints.Length;
import org.piet.forumbackend.users.core.dtos.GetUserDto;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link org.piet.forumbackend.users.groups.entities.UserGroup}
 */
@Value
public class CreateUserGroupDto implements Serializable {
    @NotNull
    Set<GetUserDto> members;
    @NotBlank
    @Length(min = 3, max = 50)
    String name;
}