package org.piet.forumbackend.security;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link org.piet.forumbackend.users.entities.User}
 */
@Value
public class SecurityUserDto implements Serializable {
    Long id;
    String username;
}