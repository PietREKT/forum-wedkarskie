package org.piet.forumbackend.users.core.services;

import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.globals.pagination.PaginationDto;
import org.piet.forumbackend.users.core.dtos.requests.RegisterUserDto;
import org.piet.forumbackend.users.core.dtos.responses.ListUserDto;
import org.piet.forumbackend.users.core.entities.Role;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    void checkIsMuted(User user);

    User getUserByUsername(String username) throws NotFoundException;

    User getCurrentUser() throws UserNotLoggedInException;

    Optional<User> getUserByUsernameOpt(String username) throws NotFoundException;

    User getUserById(UUID id) throws NotFoundException;

    User registerUser(RegisterUserDto dto);

    User getUserFromAuth(Authentication auth) throws UserNotLoggedInException;

    User getUserFromAuthOrNull(Authentication auth);

    Optional<User> getUserByIdOpt(UUID id);

    void banUser(UUID userId, Instant until, String reason, User currentUser) throws NotFoundException;
    default void permBanUser(UUID userId, String reason, User currentUser) throws NotFoundException {
        banUser(userId, Instant.MAX, reason, currentUser);
    }
    void unbanUser(UUID userId) throws NotFoundException;
    Page<ListUserDto> getBannedUsers(Pageable pageable);
    default Page<ListUserDto> getBannedUsers(PaginationDto pagination){
        return getMutedUsers(pagination.toPageable());
    }

    void muteUser(UUID userId, Instant until, String reason, User currentUser) throws NotFoundException;
    void unmuteUser(UUID userId) throws NotFoundException;
    Page<ListUserDto> getMutedUsers(Pageable pageable);
    default Page<ListUserDto> getMutedUsers(PaginationDto pagination){
        return getMutedUsers(pagination.toPageable());
    }

    User getCurrentUserOrNull();

    Page<ListUserDto> getAllUsers(PaginationDto pagination);

    void changeUserRole(UUID userToChange, Role newRole) throws NotFoundException, UserNotLoggedInException;

    List<ListUserDto> searchByUsernamePrefix(String prefix);
}
