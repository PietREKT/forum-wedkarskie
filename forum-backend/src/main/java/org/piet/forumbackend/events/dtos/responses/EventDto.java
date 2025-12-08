package org.piet.forumbackend.events.dtos.responses;

import lombok.Value;
import org.hibernate.validator.constraints.Length;
import org.piet.forumbackend.fishing_spots.dtos.responses.FishingSpotDto;
import org.piet.forumbackend.users.core.dtos.responses.UserDto;
import org.piet.forumbackend.users.groups.dtos.responses.UserGroupDto;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

/**
 * DTO for {@link org.piet.forumbackend.events.entites.Event}
 */
@Value
public class EventDto implements Serializable {
    Long id;
    @Length(max = 100)
    String name;
    @Length(max = 2000)
    String description;
    Instant startsAt;
    Instant endsAt;
    FishingSpotDto location;
    UserDto creator;
    UserGroupDto group;
    Set<EventParticipantDto> userEvents;
    boolean isParticipating;
}