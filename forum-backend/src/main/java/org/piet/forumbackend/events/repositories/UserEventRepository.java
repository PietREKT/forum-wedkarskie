package org.piet.forumbackend.events.repositories;

import org.piet.forumbackend.events.entites.Event;
import org.piet.forumbackend.events.entites.UserEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserEventRepository extends JpaRepository<UserEvent, Long> {
    Page<UserEvent> findAllByEvent(Event event, Pageable pageable);

    Page<UserEvent> findAllByUser_Id(UUID userId, Pageable pageable);

    @Query("""
        select distinct ue from UserEvent ue where ue.user.id = :userId
            and (
                    ue.event.startsAt >= :now or
                        (ue.event.startsAt <= :now and ue.event.endsAt >= :now)
                )
            order by ue.event.startsAt asc
    """)
    Page<UserEvent> findAllByUser_IdFuture(@Param("userId") UUID userId,
                                           @Param("now") Instant now,
                                           Pageable pageable);

    Optional<UserEvent> findByUser_IdAndEvent_Id(UUID userId, Long eventId);

    boolean existsByUser_IdAndEvent_Id(UUID userId, Long eventId);

    Page<UserEvent> findAllByEvent_Id(Long eventId, Pageable pageable);
}
