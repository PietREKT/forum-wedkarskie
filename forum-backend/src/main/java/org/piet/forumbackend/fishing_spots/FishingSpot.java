package org.piet.forumbackend.fishing_spots;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Point;
import org.piet.forumbackend.content.VerificationStatus;
import org.piet.forumbackend.fish.entities.Fish;
import org.piet.forumbackend.users.entities.User;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class FishingSpot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @Column(columnDefinition = "geometry(Point, 4326)")
    private Point location;

    @Enumerated(EnumType.STRING)
    private FISHING_SPOT_TYPE type;

    @Enumerated(EnumType.STRING)
    private VerificationStatus verificationStatus = VerificationStatus.IN_REVIEW;

    private String statuteUrl;

    @ManyToMany
    @JoinTable(
            name = "fishing_spots_managers",
            joinColumns = @JoinColumn(name = "spot_id"),
            inverseJoinColumns = @JoinColumn(name = "owner_id")
    )
    private List<User> managers;

    @ManyToMany
    @JoinTable(
            name = "fishing_spots_fish",
            joinColumns = @JoinColumn(name = "spot_id"),
            inverseJoinColumns = @JoinColumn(name = "fish_id")
    )
    private List<Fish> fish;

    public enum FISHING_SPOT_TYPE{
        PRIVATE,
        PUBLIC
    }
}
