package org.piet.forumbackend.notifications.factory;

import org.piet.forumbackend.events.entites.Event;
import org.piet.forumbackend.notifications.entities.Notification;
import org.piet.forumbackend.notifications.entities.enums.NotificationResourceType;
import org.piet.forumbackend.notifications.entities.enums.NotificationType;
import org.piet.forumbackend.notifications.repositories.NotificationRepository;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.groups.entities.UserGroup;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class NotificationFactoryImpl implements NotificationFactory {
    private final MessageSource messageSource;
    private final NotificationRepository notificationRepository;

    public NotificationFactoryImpl(MessageSource messageSource,
                                   NotificationRepository notificationRepository) {
        this.messageSource = messageSource;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Notification eventStartingSoon(User target, Event event) {
        Notification notification = new Notification();
        notification.setTarget(target);
        notification.setNotificationType(NotificationType.EVENT_STARTING_SOON);
        notification.setResourceId(event.getId().toString());
        notification.setNotificationResourceType(NotificationResourceType.EVENT);
        notification.setMessage(
                messageSource.getMessage("notifications.event_starting_soon",
                        null,
                        LocaleContextHolder.getLocale())
        );
        return notificationRepository.save(notification);
    }

    @Override
    public Notification friendInvitation(User target, User inviter) {
        Notification notification = new Notification();
        notification.setTarget(target);
        notification.setNotificationType(NotificationType.FRIEND_INVITATION);
        notification.setNotificationResourceType(NotificationResourceType.FRIEND_INVITATION);
        notification.setMessage(
                messageSource.getMessage("notifications.friend_invitation",
                        new Object[]{inviter.getUsername()},
                        LocaleContextHolder.getLocale())
        );
        return notificationRepository.save(notification);
    }

    @Override
    public Notification friendInvitationAccepted(User target, User accepter) {
        Notification notification = new Notification();
        notification.setTarget(target);
        notification.setNotificationType(NotificationType.FRIEND_INVITATION_ACCEPTED);
        notification.setNotificationResourceType(NotificationResourceType.FRIEND_INVITATION);
        notification.setMessage(
                messageSource.getMessage("notifications.friend_invitation_accepted",
                        new Object[]{accepter.getUsername()},
                        LocaleContextHolder.getLocale())
        );
        return notificationRepository.save(notification);
    }

    @Override
    public Notification acceptedIntoGroup(User target, UserGroup userGroup) {
        Notification notification = new Notification();
        notification.setTarget(target);
        notification.setNotificationType(NotificationType.ACCEPTED_INTO_GROUP);
        notification.setResourceId(userGroup.getId().toString());
        notification.setNotificationResourceType(NotificationResourceType.USER_GROUP);
        notification.setMessage(
                messageSource.getMessage("notifications.accepted_into_group",
                        new Object[]{userGroup.getName()},
                        LocaleContextHolder.getLocale())
        );
        return notificationRepository.save(notification);
    }

    @Override
    public Notification systemMessage(User target, String message) {
        Notification notification = new Notification();
        notification.setTarget(target);
        notification.setNotificationType(NotificationType.SYSTEM_MESSAGE);
        notification.setMessage(message);
        return notificationRepository.save(notification);
    }
}
