package org.piet.forumbackend.users;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.groups.entities.UserGroup;
import org.piet.forumbackend.users.groups.repositories.UserGroupRepository;
import org.piet.forumbackend.users.groups.services.UserGroupServiceImpl;
import org.springframework.context.MessageSource;

import java.nio.file.AccessDeniedException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // keep Mockito from whining about messageSource
class UserGroupServiceImplTest {

    @Mock
    private UserGroupRepository userGroupRepository;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private UserGroupServiceImpl userGroupService;

    @BeforeEach
    void setupMessageSource() {
        // Keep it simple: return the code as the message
        lenient().when(messageSource.getMessage(anyString(), any(), any()))
                .thenAnswer(inv -> inv.getArgument(0, String.class));
    }

    private User mockUser(String label) {
        User user = mock(User.class);
        when(user.toLogStringShort()).thenReturn(label);
        when(user.equalsUser(any(User.class))).thenAnswer(inv -> {
            User other = inv.getArgument(0);
            return user == other;
        });
        return user;
    }

    // ---------- getById ----------

    @Test
    void getById_ShouldThrow_WhenGroupNotFound() {
        UUID id = UUID.randomUUID();
        when(userGroupRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userGroupService.getById(id));
    }

    @Test
    void getById_ShouldReturn_WhenGroupExists() throws NotFoundException {
        UUID id = UUID.randomUUID();
        UserGroup group = new UserGroup();
        when(userGroupRepository.findById(id)).thenReturn(Optional.of(group));

        UserGroup result = userGroupService.getById(id);

        assertSame(group, result);
    }

    // ---------- addMemberCandidate ----------

    @Test
    void addMemberCandidate_ShouldAddCandidateAndSave() {
        UserGroup group = mock(UserGroup.class);
        User candidate = mockUser("candidate");
        User currentUser = mockUser("current");

        userGroupService.addMemberCandidate(group, candidate, currentUser);

        verify(group).addMemberCandidate(candidate);
        verify(userGroupRepository).save(group);
    }

    // ---------- rejectMemberCandidate ----------

    @Test
    void rejectMemberCandidate_ShouldThrow_WhenCurrentUserIsNotAdmin() throws AccessDeniedException {
        UserGroup group = mock(UserGroup.class);
        User candidate = mockUser("candidate");
        User currentUser = mockUser("current");

        // desired semantics: non-admin => AccessDenied
        when(group.isAdmin(currentUser)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> userGroupService.rejectMemberCandidate(group, candidate, currentUser));
    }

    @Test
    void rejectMemberCandidate_ShouldRemoveCandidateAndSave_WhenCurrentUserIsAdmin() throws Exception {
        UserGroup group = mock(UserGroup.class);
        User candidate = mockUser("candidate");
        User currentUser = mockUser("admin");

        when(group.isAdmin(currentUser)).thenReturn(true);

        userGroupService.rejectMemberCandidate(group, candidate, currentUser);

        verify(group).removeMemberCandidate(candidate);
        verify(userGroupRepository).save(group);
    }

    // ---------- addMember ----------

    @Test
    void addMember_ShouldThrow_WhenCurrentUserIsNotAdmin() throws AccessDeniedException {
        UserGroup group = mock(UserGroup.class);
        User candidate = mockUser("candidate");
        User currentUser = mockUser("current");

        when(group.isAdmin(currentUser)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> userGroupService.addMember(group, candidate, currentUser));
    }

    @Test
    void addMember_ShouldAddMemberAndSave_WhenCurrentUserIsAdmin() throws Exception {
        UserGroup group = mock(UserGroup.class);
        User candidate = mockUser("candidate");
        User currentUser = mockUser("admin");

        when(group.isAdmin(currentUser)).thenReturn(true);

        userGroupService.addMember(group, candidate, currentUser);

        verify(group).addMember(candidate);
        verify(userGroupRepository).save(group);
    }

    // ---------- removeMember ----------

    @Test
    void removeMember_ShouldThrow_WhenRemovingAnotherUserAndCurrentUserIsNotAdmin() throws AccessDeniedException {
        UserGroup group = mock(UserGroup.class);
        User member = mockUser("member");
        User currentUser = mockUser("current");

        // member != currentUser
        when(member.equalsUser(currentUser)).thenReturn(false);
        when(group.isAdmin(currentUser)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> userGroupService.removeMember(group, member, currentUser));
    }

    @Test
    void removeMember_ShouldRemoveAdminRoleAndMember_WhenAdminRemovesAdmin() throws Exception {
        UserGroup group = mock(UserGroup.class);
        User member = mockUser("member");
        User currentUser = mockUser("admin");

        when(member.equalsUser(currentUser)).thenReturn(false);
        when(group.isAdmin(currentUser)).thenReturn(true);
        when(group.isAdmin(member)).thenReturn(true);

        userGroupService.removeMember(group, member, currentUser);

        verify(group).removeAdmin(member);
        verify(group).removeMember(member);
        verify(userGroupRepository).save(group);
    }

    // ---------- createUserGroup ----------

    @Test
    void createUserGroup_ShouldSetOwnerMembersAndNameAndSave() {
        User creator = mockUser("creator");
        User member1 = mockUser("m1");
        User member2 = mockUser("m2");
        List<User> members = List.of(member1, member2);

        ArgumentCaptor<UserGroup> captor = ArgumentCaptor.forClass(UserGroup.class);
        when(userGroupRepository.save(any(UserGroup.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        UserGroup result = userGroupService.createUserGroup(creator, members, "Fishing Legends");

        verify(userGroupRepository).save(captor.capture());
        UserGroup saved = captor.getValue();

        assertEquals("Fishing Legends", saved.getName());
        assertEquals(creator, saved.getOwner());
        assertTrue(saved.getMembers().containsAll(members));
        // depending on your entity, owner may or may not be included by default
        assertSame(result, saved);
    }

    // ---------- deleteUserGroup ----------

    @Test
    void deleteUserGroup_ShouldThrow_WhenCurrentUserIsNotOwner() throws AccessDeniedException {
        UserGroup group = mock(UserGroup.class);
        User currentUser = mockUser("current");

        when(group.isOwner(currentUser)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> userGroupService.deleteUserGroup(group, currentUser));

        verify(userGroupRepository, never()).delete(group);
    }

    @Test
    void deleteUserGroup_ShouldDelete_WhenCurrentUserIsOwner() throws Exception {
        UserGroup group = mock(UserGroup.class);
        User currentUser = mockUser("owner");

        when(group.isOwner(currentUser)).thenReturn(true);

        userGroupService.deleteUserGroup(group, currentUser);

        verify(userGroupRepository).delete(group);
    }

    // ---------- addAdmin ----------

    @Test
    void addAdmin_ShouldThrow_WhenCurrentUserIsNotOwner() throws AccessDeniedException {
        UserGroup group = mock(UserGroup.class);
        User newAdmin = mockUser("newAdmin");
        User currentUser = mockUser("current");

        when(group.isOwner(currentUser)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> userGroupService.addAdmin(group, newAdmin, currentUser));

        verify(group, never()).getAdmins();
        verify(userGroupRepository, never()).save(group);
    }

    @Test
    void addAdmin_ShouldAddToAdminsAndSave_WhenCurrentUserIsOwner() throws Exception {
        UserGroup group = mock(UserGroup.class);
        User newAdmin = mockUser("newAdmin");
        User currentUser = mockUser("owner");

        when(group.isOwner(currentUser)).thenReturn(true);

        Set<User> admins = new HashSet<>();
        when(group.getAdmins()).thenReturn(admins);

        userGroupService.addAdmin(group, newAdmin, currentUser);

        assertTrue(admins.contains(newAdmin));
        verify(userGroupRepository).save(group);
    }

    // ---------- removeAdmin ----------

    @Test
    void removeAdmin_ShouldThrow_WhenCurrentUserIsNotOwner() throws AccessDeniedException {
        UserGroup group = mock(UserGroup.class);
        User toRemove = mockUser("toRemove");
        User currentUser = mockUser("current");

        when(group.isOwner(currentUser)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> userGroupService.removeAdmin(group, toRemove, currentUser));

        verify(group, never()).getAdmins();
        verify(userGroupRepository, never()).save(group);
    }

    @Test
    void removeAdmin_ShouldRemoveFromAdminsAndSave_WhenCurrentUserIsOwner() throws Exception {
        UserGroup group = mock(UserGroup.class);
        User toRemove = mockUser("toRemove");
        User currentUser = mockUser("owner");

        when(group.isOwner(currentUser)).thenReturn(true);

        Set<User> admins = new HashSet<>(List.of(toRemove));
        when(group.getAdmins()).thenReturn(admins);

        userGroupService.removeAdmin(group, toRemove, currentUser);

        assertFalse(admins.contains(toRemove));
        verify(userGroupRepository).save(group);
    }

    // ---------- transferOwnership ----------

    @Test
    void transferOwnership_ShouldThrow_WhenOldOwnerIsNotActualOwner() throws AccessDeniedException {
        UserGroup group = mock(UserGroup.class);
        User oldOwner = mockUser("old");
        User newOwner = mockUser("new");

        when(group.isOwner(oldOwner)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> userGroupService.transferOwnership(group, oldOwner, newOwner, true));

        verify(userGroupRepository, never()).save(group);
    }

    @Test
    void transferOwnership_ShouldSetNewOwnerAddAdminAndOptionallyRemoveOldAdmin() throws Exception {
        UserGroup group = mock(UserGroup.class);
        User oldOwner = mockUser("oldOwner");
        User newOwner = mockUser("newOwner");

        when(group.isOwner(oldOwner)).thenReturn(true);
        when(group.isAdmin(newOwner)).thenReturn(false);

        userGroupService.transferOwnership(group, oldOwner, newOwner, true);

        // owner set to new owner (called twice in impl, we just care it's called with newOwner)
        verify(group, atLeastOnce()).setOwner(newOwner);
        // new owner becomes admin if not admin
        verify(group).addAdmin(newOwner);
        // old owner removed from admins because removeFromAdmins = true
        verify(group).removeAdmin(oldOwner);
        verify(userGroupRepository).save(group);
    }

    @Test
    void transferOwnership_ShouldNotRemoveOldOwnerFromAdmins_WhenFlagFalse() throws Exception {
        UserGroup group = mock(UserGroup.class);
        User oldOwner = mockUser("oldOwner");
        User newOwner = mockUser("newOwner");

        when(group.isOwner(oldOwner)).thenReturn(true);
        when(group.isAdmin(newOwner)).thenReturn(true); // already admin

        userGroupService.transferOwnership(group, oldOwner, newOwner, false);

        verify(group, atLeastOnce()).setOwner(newOwner);
        // no addAdmin because already admin
        verify(group, never()).addAdmin(newOwner);
        // no removeAdmin on oldOwner
        verify(group, never()).removeAdmin(oldOwner);
        verify(userGroupRepository).save(group);
    }
}
