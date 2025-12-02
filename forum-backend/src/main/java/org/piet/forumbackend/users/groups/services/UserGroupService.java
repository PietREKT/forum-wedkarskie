package org.piet.forumbackend.users.groups.services;

import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.groups.entities.UserGroup;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

public interface UserGroupService {
    UserGroup getById(UUID id) throws NotFoundException;

    void addMemberCandidate(UserGroup group, User memberCandidate, User currentUser) throws AccessDeniedException;
    default void addMemberCandidate(UUID groupId, User memberCandidate, User currentUser) throws NotFoundException, AccessDeniedException {
        var group = getById(groupId);
        addMemberCandidate(group, memberCandidate, currentUser);
    }

    void rejectMemberCandidate(UserGroup group, User memberCandidate, User currentUser) throws AccessDeniedException;
    default void rejectMemberCandidate(UUID groupId, User memberCandidate, User currentUser) throws NotFoundException, AccessDeniedException {
        var group = getById(groupId);
        rejectMemberCandidate(group, memberCandidate, currentUser);
    }

    void addMember(UserGroup group, User newMember, User currentUser) throws AccessDeniedException;
    default void addMember(UUID groupId, User newMember, User currentUser) throws NotFoundException, AccessDeniedException {
        var group = getById(groupId);
        addMember(group, newMember, currentUser);
    };

    void removeMember(UserGroup group, User member, User currentUser) throws AccessDeniedException;
    default void removeMember(UUID groupId, User member, User currentUser) throws NotFoundException, AccessDeniedException {
        var group = getById(groupId);
        removeMember(group, member, currentUser);
    }
    default void leave(UUID groupId, User currentUser) throws AccessDeniedException, NotFoundException {
        removeMember(groupId, currentUser, currentUser);
    }

    UserGroup createUserGroup(User creator, List<User> members, String name);

    void deleteUserGroup(UserGroup userGroup, User currentUser) throws AccessDeniedException;
    default void deleteUserGroup(UUID groupId, User currentUser) throws NotFoundException, AccessDeniedException {
        var group = getById(groupId);
        deleteUserGroup(group, currentUser);
    }

    void addAdmin(UserGroup group, User newAdmin, User currentUser) throws AccessDeniedException;
    default void addAdmin(UUID groupId, User newAdmin, User currentUser) throws NotFoundException, AccessDeniedException {
        var group = getById(groupId);
        addAdmin(group, newAdmin, currentUser);
    }

    void removeAdmin(UserGroup group, User toRemove, User currentUser) throws AccessDeniedException;
    default void removeAdmin(UUID groupId, User toRemove, User currentUser) throws NotFoundException, AccessDeniedException {
        var group = getById(groupId);
        removeAdmin(group, toRemove, currentUser);
    }

    default void resignAdmin(UUID groupId, User currentUser) throws AccessDeniedException, NotFoundException {
        removeAdmin(groupId, currentUser, currentUser);
    }

    void transferOwnership(UserGroup group, User oldOwner, User newOwner, boolean removeFromAdmins) throws AccessDeniedException;
    default void transferOwnership(UUID groupId, User oldOwner, User newOwner, boolean removeFromAdmins) throws NotFoundException, AccessDeniedException {
        var group = getById(groupId);
        transferOwnership(group, oldOwner, newOwner, removeFromAdmins);
    }

    UserGroup changeName(UserGroup group, String newName, User currentUser) throws AccessDeniedException;
    default UserGroup changeName(UUID groupId, String newName, User currentUser) throws NotFoundException, AccessDeniedException {
        var group = getById(groupId);
        return changeName(group, newName, currentUser);
    }
}
