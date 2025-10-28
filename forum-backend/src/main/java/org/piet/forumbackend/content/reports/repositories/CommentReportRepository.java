package org.piet.forumbackend.content.reports.repositories;

import org.piet.forumbackend.content.reports.entities.CommentReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentReportRepository extends JpaRepository<CommentReport, Long> {
    List<CommentReport> findByReportedComment_Id(Long id);


    Page<CommentReport> findAll(Pageable pageable);
}
