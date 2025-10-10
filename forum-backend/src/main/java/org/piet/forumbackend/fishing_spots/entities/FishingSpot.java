package org.piet.forumbackend.fishing_spots.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.piet.forumbackend.users.entities.User;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class FishingSpot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;
    String localization;
    String description;

    @ManyToMany
    @JoinTable(
            name = "fishing_spots_owners",
            joinColumns = @JoinColumn(name = "spot_id"),
            inverseJoinColumns = @JoinColumn(name = "owner_id")
    )
    List<User> owners;

    @ManyToMany
    @JoinTable(
            name = "fishing_spots_fish",
            joinColumns = @JoinColumn(name = "spot_id"),
            inverseJoinColumns = @JoinColumn(name = "fish_id")
    )
    List<Fish> fish;
}
