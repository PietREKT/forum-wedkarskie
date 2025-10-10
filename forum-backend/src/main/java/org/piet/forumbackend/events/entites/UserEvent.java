package org.piet.forumbackend.events.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class UserEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "event_id")
    Event event;

    @Enumerated(EnumType.STRING)
    ATTENDANCE_STATUS status;

    public enum ATTENDANCE_STATUS {
            INVITED,
            MAYBE,
            CONFIRMED
    }
}
