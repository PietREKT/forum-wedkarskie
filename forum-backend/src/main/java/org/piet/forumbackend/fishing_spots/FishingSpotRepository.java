package org.piet.forumbackend.fishing_spots;

import org.locationtech.jts.geom.Geometry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FishingSpotRepository extends JpaRepository<FishingSpot, Long> {
    Optional<FishingSpot> findByName(String name);

    Optional<FishingSpot> findByNameIgnoreCase(String name);

    @Query("""
                select fs from FishingSpot fs where contains(:radius, fs.location) = true
            """)
    Page<FishingSpot> findByLocation(@Param("radius") Geometry radius, Pageable pageable);
}
