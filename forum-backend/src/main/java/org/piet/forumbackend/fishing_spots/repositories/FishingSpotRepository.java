package org.piet.forumbackend.fishing_spots.repositories;

import org.piet.forumbackend.fishing_spots.entities.FishingSpot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FishingSpotRepository extends JpaRepository<FishingSpot, Long> {
    Optional<FishingSpot> findByName(String name);

    Optional<FishingSpot> findByNameIgnoreCase(String name);
}
