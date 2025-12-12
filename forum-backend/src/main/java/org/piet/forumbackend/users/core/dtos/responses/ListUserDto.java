package org.piet.forumbackend.users.core.dtos.responses;

import lombok.Value;
import org.hibernate.validator.constraints.Length;
import org.piet.forumbackend.users.core.entities.User;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link User}
 */
@Value
public class ListUserDto implements Serializable {
    UUID id;
    @Length(min = 5, max = 20)
    String username;
    @Length(min = 2, max = 30)
    String name;
    @Length(min = 2, max = 30)
    String surname;

    String profilePicUrl;

}