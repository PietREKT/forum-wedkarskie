package org.piet.forumbackend.users.core.dtos.responses;

import lombok.Value;
import org.piet.forumbackend.users.core.entities.User;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link UserDto}
 */
@Value
public class ContentUserDto implements Serializable {
    String username;
    UUID id;

    public static ContentUserDto create(User user){
        return new ContentUserDto(user.getUsername(), user.getId());
    }
}