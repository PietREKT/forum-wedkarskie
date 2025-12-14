package org.piet.forumbackend.notifications.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.piet.forumbackend.notifications.entities.enums.NotificationResourceType;
import org.piet.forumbackend.notifications.entities.enums.NotificationType;
import org.piet.forumbackend.users.core.entities.User;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID notificationId;

    Instant createdAt;
    Instant readAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "target_user_id")
    User target;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    NotificationType notificationType;

    @Column(nullable = false, length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    NotificationResourceType notificationResourceType;

    private String resourceId;

    @PrePersist
    void prePersist(){
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
