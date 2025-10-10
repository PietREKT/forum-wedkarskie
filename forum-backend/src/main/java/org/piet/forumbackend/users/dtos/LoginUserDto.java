package org.piet.forumbackend.users.dtos;

import jakarta.validation.constraints.NotEmpty;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link org.piet.forumbackend.users.entities.User}
 */
@Value
public class LoginUserDto implements Serializable {
    @NotEmpty(message = "Username can't be empty")
    String username;
    @NotEmpty(message = "Password can't be empty")
    String password;
}