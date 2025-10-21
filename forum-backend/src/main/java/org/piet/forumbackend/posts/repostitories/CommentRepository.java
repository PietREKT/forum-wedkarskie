package org.piet.forumbackend.posts.repostitories;

import org.piet.forumbackend.posts.entities.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPost_Id(Long id, Pageable pageable);
}
