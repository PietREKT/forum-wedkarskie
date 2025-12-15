package org.piet.forumbackend.content.core.repositories;

import org.piet.forumbackend.content.core.entities.Tutorial;
import org.piet.forumbackend.content.core.entities.enums.VerificationStatus;
import org.piet.forumbackend.fish.entities.Fish;
import org.piet.forumbackend.fish.entities.enums.FishingMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
    @Query("""
            select t from Tutorial t
                        where :method member of t.methods
                        and t.verificationStatus=:status
            """)
    Page<Tutorial> findByMethod(@Param("method") FishingMethod method, @Param("status") VerificationStatus accepted, Pageable pageable);

    @Query("""
            select t from Tutorial t
                        where :fish member of t.fishMentioned
                        and t.verificationStatus=:status
            """)
    Page<Tutorial> findByFish(@Param("fish") Fish fish, @Param("status") VerificationStatus status, Pageable pageable);

    @Query("""
            select t from Tutorial t
                        join t.fishMentioned f
                                    where f.id in :fishIds
                                                group by t
                                                            having count(distinct f.id) >= :#{#fishIds.size()}
            """)
    Page<Tutorial> findByMultipleFish(@Param("fishIds")List<Long> fishIds, Pageable pageable);

    Page<Tutorial> findByVerificationStatus(VerificationStatus verificationStatus, Pageable pageable);
}
