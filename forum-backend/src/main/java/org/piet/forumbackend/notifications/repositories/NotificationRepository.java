package org.piet.forumbackend.notifications.repositories;

import org.piet.forumbackend.notifications.entities.Notification;
import org.piet.forumbackend.notifications.entities.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByTarget_Id(UUID targetId, Pageable pageable);

    Page<Notification> findByTarget_IdAndNotificationType(UUID targetId, NotificationType notificationType, Pageable pageable);

    Page<Notification> findByTarget_IdAndReadAtIsNull(UUID targetId, Pageable pageable);

    Page<Notification> findByTarget_IdAndNotificationTypeAndReadAtIsNull(UUID targetId, NotificationType notificationType, Pageable pageable);
}
