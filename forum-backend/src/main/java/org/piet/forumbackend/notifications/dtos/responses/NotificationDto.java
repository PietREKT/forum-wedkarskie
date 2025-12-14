package org.piet.forumbackend.notifications.dtos.responses;

import lombok.Value;
import org.piet.forumbackend.notifications.entities.enums.NotificationResourceType;
import org.piet.forumbackend.notifications.entities.enums.NotificationType;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for {@link org.piet.forumbackend.notifications.entities.Notification}
 */
@Value
public class NotificationDto implements Serializable {
    UUID notificationId;
    Instant createdAt;
    Instant readAt;
    NotificationType notificationType;
    String message;
    NotificationResourceType notificationResourceType;
    String resourceId;
}