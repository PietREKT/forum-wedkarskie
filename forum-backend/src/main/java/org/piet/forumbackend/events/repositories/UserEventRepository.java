package org.piet.forumbackend.events.repositories;

import org.piet.forumbackend.events.entites.Event;
import org.piet.forumbackend.events.entites.UserEvent;
import org.piet.forumbackend.users.core.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserEventRepository extends JpaRepository<UserEvent, Long> {
    Page<UserEvent> findAllByEvent(Event event, Pageable pageable);

    Page<UserEvent> findAllByUser(User user, Pageable pageable);

    Optional<UserEvent> findByUser_IdAndEvent_Id(UUID userId, Long eventId);

    boolean existsByUser_IdAndEvent_Id(UUID userId, Long eventId);
}
