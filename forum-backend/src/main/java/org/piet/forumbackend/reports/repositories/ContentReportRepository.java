package org.piet.forumbackend.reports.repositories;

import org.piet.forumbackend.content.entities.Content;
import org.piet.forumbackend.reports.entities.ContentReport;
import org.piet.forumbackend.reports.entities.enums.ReportReason;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ContentReportRepository extends JpaRepository<ContentReport, Long> {

    void deleteContentReportsByReported_Id(Long reportedId);

    public interface HotReportProjection{
        Long getContentId();
        Instant getCreatedAt();
        Long getReportCount();
    }

    List<ContentReport> findByReported(Content reported);
    Page<ContentReport> findByReported(Content reported, Pageable pageable);

    long countByReported(Content reported);

    void deleteByReportedAndReason(Content reported, ReportReason reason);

    @Query("""
            select r.reason as reason, count(r) as count
                       from ContentReport r
                                   where r.reported = :content
                                               group by r.reason
            """)
    List<ReasonCountProjection> countByReason(@Param("content") Content content);

    @Query("""
            select r.reason as reason, count(r) as count
                       from ContentReport r
                                   where r.reported.id = :contentId
                                               group by r.reason
            """)
    List<ReasonCountProjection> countByReason(@Param("contentId") Long contentId);

    @Query("""
            select r.reported.id as contentId, r.reported.createdAt as createdAt, count(r) as reportCount 
                        from ContentReport r
                                    where r.createdAt >= :since
                                                group by contentId, createdAt
            """)
    List<HotReportProjection> getHotReports(@Param("since") Instant since);
}
