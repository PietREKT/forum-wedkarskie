package org.piet.forumbackend.events.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.piet.forumbackend.events.entites.AttendanceStatus;

import java.io.Serializable;

/**
 * DTO for {@link org.piet.forumbackend.events.entites.UserEvent}
 */
@Value
public class EventInviteResponseDto implements Serializable {
    @NotNull
    AttendanceStatus status;
}