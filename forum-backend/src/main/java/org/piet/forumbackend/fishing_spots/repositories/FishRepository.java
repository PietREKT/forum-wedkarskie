package org.piet.forumbackend.fishing_spots.repositories;

import org.piet.forumbackend.fishing_spots.entities.Fish;
import org.piet.forumbackend.fishing_spots.entities.FishingMethod;
import org.piet.forumbackend.fishing_spots.entities.WaterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FishRepository extends JpaRepository<Fish, Long> {
    Optional<Fish> findByName(String name);

    void deleteByName(String name);

    Page<Fish> findByWaterType(WaterType waterType, Pageable pageable);

    @Query("""
            select distinct f from Fish f join f.methods m where m in :methods
            """)
    Page<Fish> findByMethods(@Param("methods") List<FishingMethod> methods, Pageable pageable);
}
