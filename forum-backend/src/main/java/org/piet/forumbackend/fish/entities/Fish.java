package org.piet.forumbackend.fish.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.piet.forumbackend.fish.entities.enums.FishingMethod;
import org.piet.forumbackend.fish.entities.enums.WaterType;

import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Fish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, updatable = false)
    String name;
    String species;

    Float avgLength;
    Float avgMass;

    String photoUrl;
    String description;

    boolean isPredatory;

    @ElementCollection
    @CollectionTable(name = "fishing_methods", joinColumns = @JoinColumn(name = "fish_id"))
    @Column(name = "method")
    @Enumerated(EnumType.STRING)
    List<FishingMethod> methods;

    @Enumerated(EnumType.STRING)
    private WaterType waterType;
}
