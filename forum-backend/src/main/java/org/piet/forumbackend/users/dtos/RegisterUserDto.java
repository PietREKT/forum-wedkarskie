package org.piet.forumbackend.users.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link org.piet.forumbackend.users.entities.User}
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
    String email;
    @NotNull
    @NotEmpty
    String password;
}