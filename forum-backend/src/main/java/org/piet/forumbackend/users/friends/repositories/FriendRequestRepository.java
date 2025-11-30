package org.piet.forumbackend.users.friends.repositories;

import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.friends.entities.FriendRequest;
import org.piet.forumbackend.users.friends.entities.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, UUID> {
    boolean existsBySenderAndReceiverAndStatus(User sender, User receiver, FriendRequestStatus status);
}
