package org.piet.forumbackend.users.core.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.piet.forumbackend.users.core.entities.User;

import java.io.Serializable;

/**
 * DTO for {@link User}
 */
@Value
public class RegisterUserDto implements Serializable {
    @NotNull
    @NotEmpty
    String username;

    @NotNull
    @NotEmpty
    String name;

    @NotNull
    @NotEmpty
    String surname;

    @NotNull
    @NotEmpty
    @Email
    String email;

    @NotNull
    @NotEmpty
    String password;
}