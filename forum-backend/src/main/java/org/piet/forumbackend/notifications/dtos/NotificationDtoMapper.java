package org.piet.forumbackend.notifications.dtos;

import org.piet.forumbackend.notifications.dtos.responses.NotificationDto;
import org.piet.forumbackend.notifications.entities.Notification;

public class NotificationDtoMapper {
    public static NotificationDto toNotificationDto(Notification notification){
        return new NotificationDto(
                notification.getNotificationId(),
                notification.getCreatedAt(),
                notification.getReadAt(),
                notification.getNotificationType(),
                notification.getMessage(),
                notification.getNotificationResourceType(),
                notification.getResourceId()
        );
    }
}
