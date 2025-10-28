package org.piet.forumbackend.content.posts.repostitories;

import org.piet.forumbackend.content.posts.entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}
