package org.piet.forumbackend.users.groups.repositories;

import org.piet.forumbackend.users.groups.entities.UserGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserGroupRepository extends JpaRepository<UserGroup, UUID> {
}
