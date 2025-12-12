package org.piet.forumbackend.fishing_spots.opinions.core.repositories;

import org.piet.forumbackend.fishing_spots.opinions.core.entities.FishingSpotOpinion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FishingSpotOpinionRepository extends JpaRepository<FishingSpotOpinion, Long> {

    Page<FishingSpotOpinion> findAllBySpot_Id(Long spotId, Pageable pageable);
}
