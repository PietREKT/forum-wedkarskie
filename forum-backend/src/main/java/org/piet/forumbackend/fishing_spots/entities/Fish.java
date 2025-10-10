package org.piet.forumbackend.fishing_spots.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Fish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;
    String species;

    Float avgLength;
    Float avgMass;
}
