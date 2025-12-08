package org.piet.forumbackend.users.core.dtos.requests.admins;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.piet.forumbackend.users.core.entities.Role;

import java.io.Serializable;

/**
 * DTO for {@link org.piet.forumbackend.users.core.entities.User}
 */
@Value
public class ChangeUserRoleDto implements Serializable {
    @NotNull
    Role role;
}