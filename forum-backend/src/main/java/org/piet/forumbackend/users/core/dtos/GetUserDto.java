package org.piet.forumbackend.users.core.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.piet.forumbackend.users.core.entities.User;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link User}
 */
@Value
public class GetUserDto implements Serializable {
    @NotNull
    UUID id;
}