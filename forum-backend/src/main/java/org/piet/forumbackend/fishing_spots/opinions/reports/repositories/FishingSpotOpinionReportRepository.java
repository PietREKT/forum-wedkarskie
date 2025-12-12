package org.piet.forumbackend.fishing_spots.opinions.reports.repositories;

import org.piet.forumbackend.content.reports.repositories.ReasonCountProjection;
import org.piet.forumbackend.fishing_spots.opinions.reports.entities.FishingSpotOpinionReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface FishingSpotOpinionReportRepository extends JpaRepository<FishingSpotOpinionReport, UUID> {
    void deleteAllByOpinion_Id(Long opinionId);

    long countByOpinion_Id(Long opinionId);

    interface HotOpinionProjection{
        Long getOpinionId();
        Long getReportCount();
        Instant getCreatedAt();
    }

    @Query("""
            select opr.reason as reason, count(opr) as count
                        from FishingSpotOpinionReport opr
                                    where opr.opinion.id = :opinionId
                                                group by opr.reason
                                                            order by count desc
            """)
    List<ReasonCountProjection> countByReason(@Param("opinionId") Long opinionId);

    @Query("""
            select r.opinion.id as opinionId,
                        r.opinion.createdAt as createdAt,
                                     count(r) as reportCount
                                                 from FishingSpotOpinionReport r
                        where r.createdAt >= :since
                                    group by r.opinion.id, r.opinion.createdAt
                                                order by createdAt desc
            """)
    List<HotOpinionProjection> findAllHotOpinions(@Param("since") Instant since);

    Page<FishingSpotOpinionReport> findAllByOpinion_Id(Long opinionId, Pageable pageable);
}
