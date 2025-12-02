package org.piet.forumbackend.users.core.dtos.responses;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.piet.forumbackend.users.core.entities.Role;

import java.io.Serializable;

/**
 * DTO for {@link Role}
 */
@Value
public class RoleDto implements Serializable {
    @NotNull
    @NotEmpty
    @NotBlank
    String name;
}