package org.piet.forumbackend.notifications.services;

import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.piet.forumbackend.notifications.dtos.NotificationDtoMapper;
import org.piet.forumbackend.notifications.dtos.responses.NotificationDto;
import org.piet.forumbackend.notifications.entities.enums.NotificationType;
import org.piet.forumbackend.notifications.repositories.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService{
    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Page<NotificationDto> getNotificationsForUser(UUID userId, PaginationDto pagination) {
        return notificationRepository.findByTarget_Id(userId, pagination.toPageable(Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(NotificationDtoMapper::toNotificationDto);
    }

    @Override
    public Page<NotificationDto> getUnreadNotificationsForUser(UUID userId, PaginationDto pagination) {
        return notificationRepository.findByTarget_IdAndReadAtIsNull(userId, pagination.toPageable(Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(NotificationDtoMapper::toNotificationDto);
    }

    @Override
    public Page<NotificationDto> getNotificationsForUserByType(UUID userId, NotificationType type, PaginationDto pagination) {
        return notificationRepository.findByTarget_IdAndNotificationType(userId, type, pagination.toPageable(Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(NotificationDtoMapper::toNotificationDto);
    }

    @Override
    public Page<NotificationDto> getUnreadNotificationsForUserByType(UUID userId, NotificationType type, PaginationDto pagination) {
        return notificationRepository.findByTarget_IdAndNotificationTypeAndReadAtIsNull(userId, type, pagination.toPageable(Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(NotificationDtoMapper::toNotificationDto);
    }

    @Override
    @Transactional
    public void markNotificationAsRead(UUID notificationId) {
        notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException("Notification with that ID doesn't exist!"))
                .setReadAt(Instant.now());
    }

    @Override
    public void deleteNotification(UUID notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}
