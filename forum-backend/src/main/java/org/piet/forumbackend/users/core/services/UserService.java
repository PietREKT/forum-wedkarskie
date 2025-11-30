package org.piet.forumbackend.users.core.services;

import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.users.core.dtos.RegisterUserDto;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.UUID;

public interface UserService {
    User getUserByUsername(String username) throws NotFoundException;

    User getCurrentUser() throws UserNotLoggedInException;

    Optional<User> getUserByUsernameOpt(String username) throws NotFoundException;

    User getUserById(UUID id) throws NotFoundException;

    User registerUser(RegisterUserDto dto);

    User getUserFromAuth(Authentication auth) throws UserNotLoggedInException;

    User getUserFromAuthOrNull(Authentication auth);

    Optional<User> getUserByIdOpt(UUID id);
}
