package org.piet.forumbackend.events.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.piet.forumbackend.users.core.entities.User;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user_events",
uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "event_id"}))
public class UserEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Enumerated(EnumType.STRING)
            @Column(nullable = false)
    AttendanceStatus status;

    public String toLogString(){
        return "UserEvent: {" +
                " id: " + id +
                ", event: " + event.toLogStringShort() +
                ", user: " + user.toLogStringShort() +
                " }";
    }
}
