package org.piet.forumbackend.events.entites;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.piet.forumbackend.fishing_spots.entities.FishingSpot;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.groups.entities.UserGroup;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Length(max = 100)
    private String name;
    @Length(max = 2000)
    private String description;

    private Instant startsAt;

    private Instant endsAt;

    @ManyToOne(targetEntity = FishingSpot.class,
            fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id")
    private FishingSpot location;

    @ManyToOne(targetEntity = User.class,
            fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @ManyToOne(targetEntity = UserGroup.class,
            fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private UserGroup group;

    @OneToMany(
            mappedBy = "event",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<UserEvent> userEvents = new HashSet<>();
}
