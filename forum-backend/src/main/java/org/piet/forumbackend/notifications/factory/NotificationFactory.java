package org.piet.forumbackend.notifications.factory;

import org.piet.forumbackend.events.entites.Event;
import org.piet.forumbackend.notifications.entities.Notification;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.groups.entities.UserGroup;

public interface NotificationFactory {
    Notification eventStartingSoon(User target, Event event);

    Notification friendInvitation(User target, User inviter);

    Notification friendInvitationAccepted(User target, User accepter);

    Notification acceptedIntoGroup(User target, UserGroup userGroup);

    Notification systemMessage(User target, String message);
}
