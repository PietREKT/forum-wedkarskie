package org.piet.forumbackend.users.dtos;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link UserDto}
 */
@Value
public class PostUserDto implements Serializable {
    String username;
}