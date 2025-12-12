package org.piet.forumbackend.content.core.repositories;

import org.piet.forumbackend.content.core.entities.ContentVote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentVoteRepository extends JpaRepository<ContentVote, Long> {
}