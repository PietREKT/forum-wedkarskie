package org.piet.forumbackend.users.core.repos;

import org.piet.forumbackend.users.core.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    Page<User> findByBannedUntilAfter(Instant bannedUntilAfter, Pageable pageable);

    Page<User> findByMutedUntilAfter(Instant mutedUntilAfter, Pageable pageable);
}
