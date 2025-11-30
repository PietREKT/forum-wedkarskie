package org.piet.forumbackend.users.core.dtos;

import lombok.Value;
import org.piet.forumbackend.users.core.entities.User;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link User}
 */
@Value
public class UserDto implements Serializable {
    UUID id;
    String username;
    String name;
    String surname;
    String email;
    String phone;
    RoleDto role;
}