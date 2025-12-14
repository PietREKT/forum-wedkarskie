package org.piet.forumbackend.users.core.dtos.responses;

import lombok.Value;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for {@link org.piet.forumbackend.users.core.entities.User}
 */
@Value
public class PunishedUserListDto implements Serializable {
    UUID id;
    @Length(min = 4, max = 20)
    String username;
    @Length(min = 2, max = 30)
    String name;
    @Length(min = 2, max = 30)
    String surname;
    Instant bannedUntil;
    Instant mutedUntil;
    String banReason;
    String profilePicUrl;
}