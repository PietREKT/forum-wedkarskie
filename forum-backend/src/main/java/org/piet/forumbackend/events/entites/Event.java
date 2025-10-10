package org.piet.forumbackend.events.entites;

import jakarta.persistence.*;
import org.piet.forumbackend.fishing_spots.entities.FishingSpot;

import java.time.Instant;

@Entity
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String name;

    String description;

    Instant startsAt;

    Instant endsAt;

    @ManyToOne(targetEntity = FishingSpot.class)
    FishingSpot location;
}
