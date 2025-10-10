package org.piet.forumbackend.users.repos;

import org.piet.forumbackend.users.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
