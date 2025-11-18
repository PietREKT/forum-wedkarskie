package org.piet.forumbackend.content.repositories;

import org.piet.forumbackend.content.entities.Content;
import org.piet.forumbackend.content.entities.enums.ContentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentRepository extends JpaRepository<Content, Long> {
    Page<Content> findByParent(Content parent, Pageable pageable);

    Page<Content> findByContentType(ContentType contentType, Pageable pageable);
}
