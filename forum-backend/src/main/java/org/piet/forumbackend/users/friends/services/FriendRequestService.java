package org.piet.forumbackend.users.friends.services;

import org.piet.forumbackend.globals.exceptions.BadRequestException;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.friends.entities.FriendRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface FriendRequestService {
    FriendRequest getById(UUID id) throws NotFoundException;

    @Transactional
    public FriendRequest sendFriendRequest(User from, User to) throws BadRequestException;

    @Transactional
    void acceptFriendRequest(UUID requestId, User currentUser) throws NotFoundException, BadRequestException;

    @Transactional
    void rejectFriendRequest(UUID requestId, User currentUser) throws NotFoundException, BadRequestException;

    void cancelFriendRequest(UUID requestId, User currentUser) throws NotFoundException, BadRequestException;
}
