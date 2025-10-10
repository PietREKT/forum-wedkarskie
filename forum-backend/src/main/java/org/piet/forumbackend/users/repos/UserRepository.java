package org.piet.forumbackend.users.repos;

import org.piet.forumbackend.users.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
