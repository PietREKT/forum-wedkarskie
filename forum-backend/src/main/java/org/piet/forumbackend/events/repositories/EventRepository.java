package org.piet.forumbackend.events.repositories;

import org.piet.forumbackend.events.entites.AttendanceStatus;
import org.piet.forumbackend.events.entites.Event;
import org.piet.forumbackend.users.core.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findAllByLocation_Id(Long locationId, Pageable pageable);

    @Query("""
            select e from Event e where e.location.id = :locationId
                        and (e.startsAt >= :now
                                    or (e.startsAt <= :now and e.endsAt >= :now)
                        )
            order by e.startsAt asc
            """)
    Page<Event> findAllByLocationAndFuture(@Param("locationId") Long locationId,
                                           @Param("now") Instant now,
                                           Pageable pageable);

    Page<Event> findAllByGroup_Id(UUID groupId, Pageable pageable);

    @Query("""
            select e from Event e where e.group.id = :groupId
                        and (e.startsAt >= :now
                                    or (e.startsAt <= :now and e.endsAt >= :now)
                        )
            order by e.startsAt asc
            """)
    Page<Event> findAllByGroup_IdAndFuture(@Param("groupId") UUID groupId,
                                           @Param("now") Instant now,
                                           Pageable pageable);

    Page<Event> findAllByCreator(User creator, Pageable pageable);

    @Query("""
            select e from Event e where e.creator.id = :creatorId
                        and (e.startsAt >= :now
                                    or (e.startsAt <= :now and e.endsAt <= :now)
                        )
            order by e.startsAt asc
            """)
    Page<Event> findAllByCreator_IdAndFuture(@Param("creatorId") UUID creatorId,
                                             @Param("now") Instant now,
                                             Pageable pageable);

    @Query("""
            select distinct e from Event e join UserEvent ue
                        on ue.event.id = e.id
                        where ue.user.id = :userId and ue.status in :status
                                    and (e.startsAt >= :now
                                        or (e.startsAt <= :now and e.endsAt >= :now)
                                    )
                        order by e.startsAt asc
            """)
    Page<Event> findAllByUserParticipatingAndStatus(@Param("userId") UUID userId, @Param("now") Instant now, @Param("status") List<AttendanceStatus> status, Pageable pageable);

    @Query("""
            select distinct e from Event e join UserEvent ue
                        on ue.event.id=e.id
                                    where ue.user.id=:userId and ue.status=org.piet.forumbackend.events.entites.AttendanceStatus.INVITED
                                                and e.startsAt >= :now
            """)
    Page<Event> findAllByUserInvited(@Param("userId") UUID userId, @Param("now") Instant now, Pageable pageable);

    List<Event> findTop10ByNameStartingWithIgnoreCaseOrderByNameAsc(String name);
}
