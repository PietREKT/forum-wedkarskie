package org.piet.forumbackend.notifications.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.globals.pagination.PageDto;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.piet.forumbackend.notifications.dtos.responses.NotificationDto;
import org.piet.forumbackend.notifications.entities.enums.NotificationType;
import org.piet.forumbackend.notifications.services.NotificationService;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;
import org.piet.forumbackend.users.core.services.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("${forum.api.prefix}/users/me/notifications")
@RequiredArgsConstructor
@Tag(name = "Me Controller - Notifications", description = "Endpoints for current user's notifications management.")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(@PathVariable UUID notificationId){
        notificationService.deleteNotification(notificationId);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{notificationId}")
    @Operation(summary = "Mark notification as read.")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable UUID notificationId){
        notificationService.markNotificationAsRead(notificationId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PageDto<NotificationDto>> getNotifications(
            @RequestParam(name = "unread", required = false, defaultValue = "false") boolean unread,
            @RequestParam(name = "type", required = false) NotificationType type,
            PaginationDto pagination
    ) throws UserNotLoggedInException {
        Page<NotificationDto> page;
        UUID currentUserId = userService.getCurrentUser().getId();
        if (unread && type != null){
            page = notificationService.getUnreadNotificationsForUserByType(currentUserId, type, pagination);
        } else if (type != null){
            page = notificationService.getNotificationsForUserByType(currentUserId, type, pagination);
        } else if (unread){
            page = notificationService.getUnreadNotificationsForUser(currentUserId, pagination);
        } else {
            page = notificationService.getNotificationsForUser(currentUserId, pagination);
        }

        return ResponseEntity.ok(PageDto.of(page));
    }
}
