package org.piet.forumbackend.globals.security;

import lombok.Value;
import org.piet.forumbackend.users.core.entities.User;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link User}
 */
@Value
public class SecurityUserDto implements Serializable {
    UUID id;
    String username;
}