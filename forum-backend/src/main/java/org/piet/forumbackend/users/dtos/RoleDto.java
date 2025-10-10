package org.piet.forumbackend.users.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link org.piet.forumbackend.users.entities.Role}
 */
@Value
public class RoleDto implements Serializable {
    @NotNull
    @NotEmpty
    @NotBlank
    String name;
}