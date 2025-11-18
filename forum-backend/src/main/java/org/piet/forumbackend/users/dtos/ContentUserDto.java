package org.piet.forumbackend.users.dtos;

import lombok.Value;
import org.piet.forumbackend.users.entities.User;

import java.io.Serializable;

/**
 * DTO for {@link UserDto}
 */
@Value
public class ContentUserDto implements Serializable {
    String username;

    public static ContentUserDto create(User user){
        return new ContentUserDto(user.getUsername());
    }
}