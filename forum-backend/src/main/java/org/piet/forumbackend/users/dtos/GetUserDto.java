package org.piet.forumbackend.users.dtos;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link org.piet.forumbackend.users.entities.User}
 */
@Value
public class GetUserDto implements Serializable {
    String username;
    String email;
}