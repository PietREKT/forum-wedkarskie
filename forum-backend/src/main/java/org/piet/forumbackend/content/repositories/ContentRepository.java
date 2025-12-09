package org.piet.forumbackend.content.repositories;

import org.piet.forumbackend.content.entities.Content;
import org.piet.forumbackend.content.entities.enums.ContentType;
import org.piet.forumbackend.users.core.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ContentRepository extends JpaRepository<Content, Long> {
    Page<Content> findByParent(Content parent, Pageable pageable);

    Page<Content> findByContentType(ContentType contentType, Pageable pageable);

    Page<Content> findByContentTypeAndAuthor(ContentType type, User author, Pageable pageable);

    Page<Content> findAllByGroup_Id(UUID groupId, Pageable pageable);
}
