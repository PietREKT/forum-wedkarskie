package org.piet.forumbackend.fish.repositories;

import org.piet.forumbackend.fish.entities.Fish;
import org.piet.forumbackend.fish.entities.enums.FishingMethod;
import org.piet.forumbackend.fish.entities.enums.WaterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FishRepository extends JpaRepository<Fish, Long> {
    Optional<Fish> findByName(String name);

    List<Fish> findTop10ByNameStartingWithIgnoreCaseOrderByNameAsc(String name);

    void deleteByName(String name);

    Page<Fish> findByWaterType(WaterType waterType, Pageable pageable);

    @Query("""
            select distinct f from Fish f join f.methods m where m in :methods
            """)
    Page<Fish> findByMethods(@Param("methods") List<FishingMethod> methods, Pageable pageable);

    boolean existsByName(String name);

    @Query("""
            select f from Fish f
                        where lower(f.name) like lower(concat('%', :q, '%'))
                                    order by case when
                                                lower(f.name) like lower(concat(:q, '%')) then 0 else 1 end,
                                    length(f.name)
            """)
    List<Fish> findNamesForAutocomplete(@Param("q") String query, Pageable pageable);
}
