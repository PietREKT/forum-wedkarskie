package org.piet.forumbackend.content.repositories;

import org.piet.forumbackend.content.entities.Tutorial;
import org.piet.forumbackend.content.entities.enums.VerificationStatus;
import org.piet.forumbackend.fish.entities.Fish;
import org.piet.forumbackend.fish.entities.enums.FishingMethod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TutorialRepository extends JpaRepository<Tutorial, Long> {
    @Query("""
            select t from Tutorial t
                        where :method member of t.methods
                        and t.verificationStatus=org.piet.forumbackend.content.entities.enums.VerificationStatus.ACCEPTED
            """)
    Page<Tutorial> findByMethod(@Param("method") FishingMethod method, Pageable pageable);

    @Query("""
            select t from Tutorial t
                        where :fish member of t.fishMentioned
                        and t.verificationStatus=org.piet.forumbackend.content.entities.enums.VerificationStatus.ACCEPTED
            """)
    Page<Tutorial> findByFish(@Param("fish") Fish fish, Pageable pageable);

    Page<Tutorial> findByVerificationStatus(VerificationStatus verificationStatus, Pageable pageable);
}
