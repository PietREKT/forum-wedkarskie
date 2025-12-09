package org.piet.forumbackend.fishing_spots.repositories;

import org.locationtech.jts.geom.Geometry;
import org.piet.forumbackend.content.entities.enums.VerificationStatus;
import org.piet.forumbackend.fishing_spots.entities.FishingSpot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FishingSpotRepository extends JpaRepository<FishingSpot, Long> {
    Optional<FishingSpot> findByName(String name);

    Optional<FishingSpot> findByNameIgnoreCase(String name);

    @Query("""
                select fs from FishingSpot fs where contains(:radius, fs.location) = true
                            and fs.verificationStatus=org.piet.forumbackend.content.entities.enums.VerificationStatus.ACCEPTED
            """)
    Page<FishingSpot> findByLocation(@Param("radius") Geometry radius, Pageable pageable);

    Page<FishingSpot> findByVerificationStatus(VerificationStatus verificationStatus, Pageable pageable);

    boolean existsByIdAndOwner_Id(Long id, UUID ownerId);

    List<FishingSpot> findTop10ByNameStartingWithIgnoreCaseOrderByNameAsc(String query);
}
