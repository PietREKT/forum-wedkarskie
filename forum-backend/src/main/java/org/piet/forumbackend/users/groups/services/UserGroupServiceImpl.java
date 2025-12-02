package org.piet.forumbackend.users.groups.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.groups.entities.UserGroup;
import org.piet.forumbackend.users.groups.repositories.UserGroupRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class UserGroupServiceImpl implements UserGroupService {
    private final UserGroupRepository userGroupRepository;
    private final MessageSource messageSource;

    private void checkOwner(UserGroup group, User user) throws AccessDeniedException {
        if (!group.isOwner(user))
            throw new AccessDeniedException(
                    messageSource.getMessage("error.users.groups.not_owner",
                            null,
                            LocaleContextHolder.getLocale()
                    )
            );
    }

    private void checkAdmin(UserGroup group, User user) throws AccessDeniedException {
        if (!group.isAdmin(user)) {
            throw new AccessDeniedException(
                    messageSource.getMessage("error.users.groups.not_admin",
                            null,
                            LocaleContextHolder.getLocale()
                    )
            );
        }
    }

    @Override
    public Optional<UserGroup> getByIdOpt(UUID id) {
        return userGroupRepository.findById(id);
    }

    @Override
    public UserGroup getById(UUID id) throws NotFoundException {
        return userGroupRepository.findById(id).orElseThrow(() -> new NotFoundException(
                messageSource.getMessage("error.users.groups.not_found",
                        null,
                        LocaleContextHolder.getLocale()
                )
        ));
    }

    @Override
    public UserGroup createUserGroup(User creator, List<User> members, String name) {
        UserGroup group = new UserGroup();
        group.setOwner(creator);
        group.setMembers(new HashSet<>(members));
        group.setName(name);
        log.info("{} created {}", creator.toLogStringShort(), group.toLogStringShort());
        return userGroupRepository.save(group);
    }

    @Override
    public void addMemberCandidate(UserGroup group, User memberCandidate, User currentUser) throws AccessDeniedException {
        if (group.isAdmin(currentUser)){
            addMember(group, memberCandidate, currentUser);
        }
        else {
            group.addMemberCandidate(memberCandidate);

            log.info("{} invited {} to {}", memberCandidate, currentUser, group.toLogStringShort());

            userGroupRepository.save(group);
        }
    }

    @Override
    public void rejectMemberCandidate(UserGroup group, User memberCandidate, User currentUser) throws AccessDeniedException {
        checkAdmin(group, currentUser);
        group.removeMemberCandidate(memberCandidate);

        log.info("{} rejected {}'s membership in {}", currentUser.toLogStringShort(), memberCandidate.toLogStringShort(), group.toLogStringShort());
        userGroupRepository.save(group);
    }

    @Override
    public void addMember(UserGroup group, User memberCandidate, User currentUser) throws AccessDeniedException {
        checkAdmin(group, currentUser);

        group.addMember(memberCandidate);
        log.info("{} accepted/added {} into {}", currentUser.toLogStringShort(), memberCandidate.toLogStringShort(), group.toLogStringShort());
        userGroupRepository.save(group);
    }

    @Override
    public void removeMember(UserGroup group, User member, User currentUser) throws AccessDeniedException {
        if (!member.equalsUser(currentUser)){
            checkAdmin(group, currentUser);
        }
        if (group.isAdmin(member)){
            checkOwner(group, currentUser);
            group.removeAdmin(member);
        }
        group.removeMember(member);
        log.info("{} removed {} from {}", currentUser.toLogStringShort(), member.toLogStringShort(), group.toLogStringShort());
        userGroupRepository.save(group);
    }

    @Override
    public UserGroup changeName(UserGroup group, String newName, User currentUser) throws AccessDeniedException {
        checkAdmin(group, currentUser);
        String oldName = group.getName();
        group.setName(newName);

        log.info("{} changed group's name from {} to {}.", currentUser.toLogStringShort(), oldName, group.toLogStringShort());

        return userGroupRepository.save(group);
    }

    @Override
    public void deleteUserGroup(UserGroup userGroup, User currentUser) throws AccessDeniedException {
        checkOwner(userGroup, currentUser);
        userGroupRepository.delete(userGroup);
    }

    @Override
    public void addAdmin(UserGroup group, User newAdmin, User currentUser) throws AccessDeniedException {
        checkOwner(group, currentUser);

        group.getAdmins().add(newAdmin);
        log.info("{} added {} as an admin to {}", currentUser.toLogStringShort(), newAdmin.toLogStringShort(), group.toLogStringShort());
        userGroupRepository.save(group);
    }

    @Override
    public void removeAdmin(UserGroup group, User toRemove, User currentUser) throws AccessDeniedException {
        if (!toRemove.equalsUser(currentUser)){
            checkOwner(group, currentUser);
            log.info("{} removed {} from admins of {}", currentUser.toLogStringShort(), toRemove.toLogStringShort(), group.toLogStringShort());
        } else {
            log.info("{} resigned from administering {}", currentUser.toLogStringShort(), group.toLogStringShort());
        }

        group.getAdmins().remove(toRemove);

        userGroupRepository.save(group);
    }

    @Override
    public void transferOwnership(UserGroup group, User oldOwner, User newOwner, boolean removeFromAdmins) throws AccessDeniedException {
        checkOwner(group, oldOwner);

        group.setOwner(newOwner);
        if (!group.isAdmin(newOwner)) {
            group.addAdmin(newOwner);
        }
        if (removeFromAdmins) {
            group.removeAdmin(oldOwner);
        }
        group.setOwner(newOwner);
        log.info("{} transferred ownership of {} to {}", oldOwner.toLogStringShort(), group.toLogStringShort(), newOwner.toLogStringShort());
        userGroupRepository.save(group);
    }
}
