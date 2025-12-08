package org.piet.forumbackend.events.dtos.requests;

import lombok.Value;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for {@link org.piet.forumbackend.events.entites.Event}
 */
@Value
public class CreateEventDto implements Serializable {
    @Length(max = 100)
    String name;
    @Length(max = 2000)
    String description;
    Instant startsAt;
    Instant endsAt;
    Long locationId;
    UUID groupId;
    Set<UUID> invitedUsersIds;
}