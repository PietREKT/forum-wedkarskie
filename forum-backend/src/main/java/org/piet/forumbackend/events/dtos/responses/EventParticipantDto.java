package org.piet.forumbackend.events.dtos.responses;

import lombok.Value;
import org.piet.forumbackend.events.entites.AttendanceStatus;
import org.piet.forumbackend.users.core.dtos.responses.ListUserDto;

import java.io.Serializable;

/**
 * DTO for {@link org.piet.forumbackend.events.entites.UserEvent}
 */
@Value
public class EventParticipantDto implements Serializable {
    ListUserDto user;
    AttendanceStatus status;
}