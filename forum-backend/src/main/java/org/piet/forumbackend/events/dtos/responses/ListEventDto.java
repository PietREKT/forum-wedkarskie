package org.piet.forumbackend.events.dtos.responses;

import lombok.Value;
import org.piet.forumbackend.fishing_spots.dtos.responses.FishingSpotDto;
import org.piet.forumbackend.users.groups.dtos.responses.UserGroupDto;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for {@link org.piet.forumbackend.events.entites.Event}
 */
@Value
public class ListEventDto implements Serializable {
    Long id;
    String name;
    Instant startsAt;
    Instant endsAt;
    FishingSpotDto location;
    UserGroupDto group;
}