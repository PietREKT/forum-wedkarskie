package org.piet.forumbackend.users.core.dtos.requests;

import jakarta.validation.constraints.NotEmpty;
import lombok.Value;
import org.piet.forumbackend.users.core.entities.User;

import java.io.Serializable;

/**
 * DTO for {@link User}
 */
@Value
public class LoginUserDto implements Serializable {
    @NotEmpty(message = "Username can't be empty")
    String username;
    @NotEmpty(message = "Password can't be empty")
    String password;
}