package org.piet.forumbackend.fishing_spots.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Point;
import org.piet.forumbackend.content.entities.enums.VerificationStatus;
import org.piet.forumbackend.fish.entities.Fish;
import org.piet.forumbackend.users.core.entities.User;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @ManyToOne
    User owner;

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

    @OneToMany(
            mappedBy = "spot",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private Set<FishingSpotOpinion> opinions = new HashSet<>();

    public void addOpinion(FishingSpotOpinion opinion){
        opinions.add(opinion);
    }

    public enum FISHING_SPOT_TYPE {
        PRIVATE,
        PUBLIC
    }
}
