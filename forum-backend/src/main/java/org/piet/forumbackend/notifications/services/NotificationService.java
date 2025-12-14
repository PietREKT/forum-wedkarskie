package org.piet.forumbackend.notifications.services;

import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.piet.forumbackend.notifications.dtos.responses.NotificationDto;
import org.piet.forumbackend.notifications.entities.enums.NotificationType;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface NotificationService {
    public Page<NotificationDto> getNotificationsForUser(UUID userId, PaginationDto pagination);

    public Page<NotificationDto> getUnreadNotificationsForUser(UUID userId, PaginationDto pagination);

    public Page<NotificationDto> getNotificationsForUserByType(UUID userId, NotificationType type, PaginationDto pagination);

    public Page<NotificationDto> getUnreadNotificationsForUserByType(UUID userId, NotificationType type, PaginationDto pagination);

    void markNotificationAsRead(UUID notificationId);

    void deleteNotification(UUID notificationId);
}
