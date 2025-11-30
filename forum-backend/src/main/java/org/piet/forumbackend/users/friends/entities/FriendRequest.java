package org.piet.forumbackend.users.friends.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.piet.forumbackend.users.core.entities.User;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"sender_id", "receiver_id"})
        }
)
public class FriendRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "sender_id")
    User sender;
    @ManyToOne(optional = false)
    @JoinColumn(name = "receiver_id")
    User receiver;

    @Enumerated(EnumType.STRING)
    FriendRequestStatus status;

    Instant sentAt;
    Instant respondedAt;

    @PrePersist
    void init() {
        if (sentAt == null)
            sentAt = Instant.now();
        if (status == null)
            status = FriendRequestStatus.PENDING;
    }
}
