package org.piet.forumbackend.content.reports.repositories;

import org.piet.forumbackend.content.reports.entities.PostReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostReportRepository extends JpaRepository<PostReport, Long> {
    List<PostReport> findByReportedPost_Id(Long id);

    Page<PostReport> findAll(Pageable pageable);
}
