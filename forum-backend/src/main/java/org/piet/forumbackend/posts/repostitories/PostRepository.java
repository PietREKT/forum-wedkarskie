package org.piet.forumbackend.posts.repostitories;

import org.piet.forumbackend.posts.entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
