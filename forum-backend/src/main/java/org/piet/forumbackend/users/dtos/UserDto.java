package org.piet.forumbackend.users.dtos;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link org.piet.forumbackend.users.entities.User}
 */
@Value
public class UserDto implements Serializable {
    Long id;
    String username;
    String name;
    String surname;
    String email;
    String phone;
    RoleDto role;
}