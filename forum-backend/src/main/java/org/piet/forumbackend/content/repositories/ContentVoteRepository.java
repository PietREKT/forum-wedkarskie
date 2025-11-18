package org.piet.forumbackend.content.repositories;

import org.piet.forumbackend.content.entities.ContentVote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentVoteRepository extends JpaRepository<ContentVote, Long> {
}