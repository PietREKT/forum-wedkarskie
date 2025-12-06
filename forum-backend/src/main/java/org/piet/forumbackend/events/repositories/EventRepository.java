package org.piet.forumbackend.events.repositories;

import org.piet.forumbackend.events.entites.Event;
import org.piet.forumbackend.users.core.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findAllByLocation_Id(Long locationId, Pageable pageable);

    Page<Event> findAllByGroup_Id(UUID groupId, Pageable pageable);

    Page<Event> findAllByCreator(User creator, Pageable pageable);
}
