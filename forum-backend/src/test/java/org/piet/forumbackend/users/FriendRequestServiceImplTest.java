package org.piet.forumbackend.users;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.piet.forumbackend.globals.exceptions.BadRequestException;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.friends.entities.FriendRequest;
import org.piet.forumbackend.users.friends.entities.FriendRequestStatus;
import org.piet.forumbackend.users.friends.repositories.FriendRequestRepository;
import org.piet.forumbackend.users.friends.services.FriendRequestServiceImpl;
import org.springframework.context.MessageSource;
import org.springframework.security.access.AccessDeniedException;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // chill, Mockito
class FriendRequestServiceImplTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private FriendRequestRepository friendRequestRepository;

    @InjectMocks
    private FriendRequestServiceImpl friendRequestService;

    @BeforeEach
    void setupMessageSource() {
        // lenient stub: used only in tests that hit exceptions
        lenient().when(messageSource.getMessage(anyString(), any(), any()))
                .thenAnswer(invocation -> invocation.getArgument(0, String.class));
    }

    private User createUser(UUID id) {
        User u = new User();
        u.setId(id);
        // make sure friends is not null
        u.setFriends(new HashSet<>());
        return u;
    }

    // ---------- getById ----------

    @Test
    void getById_ShouldThrow_WhenNotFound() {
        UUID id = UUID.randomUUID();
        when(friendRequestRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> friendRequestService.getById(id));
    }

    @Test
    void getById_ShouldReturn_WhenFound() throws NotFoundException {
        UUID id = UUID.randomUUID();
        FriendRequest request = new FriendRequest();
        request.setId(id);

        when(friendRequestRepository.findById(id)).thenReturn(Optional.of(request));

        FriendRequest result = friendRequestService.getById(id);
        assertSame(request, result);
    }

    // ---------- sendFriendRequest ----------

    @Test
    void sendFriendRequest_ShouldThrow_WhenSameUser() {
        User user = createUser(UUID.randomUUID());

        assertThrows(BadRequestException.class,
                () -> friendRequestService.sendFriendRequest(user, user));

        verify(friendRequestRepository, never()).save(any());
    }

    @Test
    void sendFriendRequest_ShouldThrow_WhenAlreadyFriends() {
        User from = createUser(UUID.randomUUID());
        User to = createUser(UUID.randomUUID());

        from.getFriends().add(to);

        assertThrows(BadRequestException.class,
                () -> friendRequestService.sendFriendRequest(from, to));

        verify(friendRequestRepository, never()).save(any());
    }

    @Test
    void sendFriendRequest_ShouldThrow_WhenPendingRequestExistsEitherDirection() {
        User from = createUser(UUID.randomUUID());
        User to = createUser(UUID.randomUUID());

        when(friendRequestRepository
                .existsBySenderAndReceiverAndStatus(from, to, FriendRequestStatus.PENDING))
                .thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> friendRequestService.sendFriendRequest(from, to));

        verify(friendRequestRepository, never()).save(any());
    }

    @Test
    void sendFriendRequest_ShouldCreateRequest_WhenValid() throws BadRequestException {
        User from = createUser(UUID.randomUUID());
        User to = createUser(UUID.randomUUID());

        when(friendRequestRepository
                .existsBySenderAndReceiverAndStatus(any(), any(), eq(FriendRequestStatus.PENDING)))
                .thenReturn(false);

        FriendRequest saved = new FriendRequest();
        when(friendRequestRepository.save(any(FriendRequest.class)))
                .thenAnswer(inv -> {
                    FriendRequest r = inv.getArgument(0);
                    r.setId(UUID.randomUUID());
                    return r;
                });

        FriendRequest result = friendRequestService.sendFriendRequest(from, to);

        assertNotNull(result.getId());
        assertEquals(from, result.getSender());
        assertEquals(to, result.getReceiver());
        verify(friendRequestRepository).save(any(FriendRequest.class));
    }

    // ---------- acceptFriendRequest ----------

    @Test
    void acceptFriendRequest_ShouldThrow_NotFound_WhenRequestDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(friendRequestRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> friendRequestService.acceptFriendRequest(id, createUser(UUID.randomUUID())));
    }

    @Test
    void acceptFriendRequest_ShouldThrow_WhenCurrentUserIsNotReceiver() {
        UUID id = UUID.randomUUID();
        User sender = createUser(UUID.randomUUID());
        User receiver = createUser(UUID.randomUUID());
        User other = createUser(UUID.randomUUID());

        FriendRequest request = new FriendRequest();
        request.setId(id);
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus(FriendRequestStatus.PENDING);

        when(friendRequestRepository.findById(id)).thenReturn(Optional.of(request));

        assertThrows(AccessDeniedException.class,
                () -> friendRequestService.acceptFriendRequest(id, other));
    }

    @Test
    void acceptFriendRequest_ShouldThrow_WhenNotPending() {
        UUID id = UUID.randomUUID();
        User sender = createUser(UUID.randomUUID());
        User receiver = createUser(UUID.randomUUID());

        FriendRequest request = new FriendRequest();
        request.setId(id);
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus(FriendRequestStatus.ACCEPTED);

        when(friendRequestRepository.findById(id)).thenReturn(Optional.of(request));

        assertThrows(BadRequestException.class,
                () -> friendRequestService.acceptFriendRequest(id, receiver));
    }

    @Test
    void acceptFriendRequest_ShouldAddFriendsAndSetAccepted() throws Exception {
        UUID id = UUID.randomUUID();
        User sender = createUser(UUID.randomUUID());
        User receiver = createUser(UUID.randomUUID());

        FriendRequest request = new FriendRequest();
        request.setId(id);
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus(FriendRequestStatus.PENDING);

        when(friendRequestRepository.findById(id)).thenReturn(Optional.of(request));
        when(friendRequestRepository.save(any(FriendRequest.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        friendRequestService.acceptFriendRequest(id, receiver);

        assertTrue(sender.getFriends().contains(receiver));
        assertTrue(receiver.getFriends().contains(sender));
        assertEquals(FriendRequestStatus.ACCEPTED, request.getStatus());
        assertNotNull(request.getRespondedAt());
        verify(friendRequestRepository).save(request);
    }

    // ---------- rejectFriendRequest ----------

    @Test
    void rejectFriendRequest_ShouldSetRejected() throws Exception {
        UUID id = UUID.randomUUID();
        User sender = createUser(UUID.randomUUID());
        User receiver = createUser(UUID.randomUUID());

        FriendRequest request = new FriendRequest();
        request.setId(id);
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus(FriendRequestStatus.PENDING);

        when(friendRequestRepository.findById(id)).thenReturn(Optional.of(request));
        when(friendRequestRepository.save(any(FriendRequest.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        friendRequestService.rejectFriendRequest(id, receiver);

        assertEquals(FriendRequestStatus.REJECTED, request.getStatus());
        assertNotNull(request.getRespondedAt());
        verify(friendRequestRepository).save(request);
    }

    // ---------- cancelFriendRequest (current behavior: receiver only) ----------

    @Test
    void cancelFriendRequest_ShouldThrow_NotFound_WhenRequestDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(friendRequestRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> friendRequestService.cancelFriendRequest(id, createUser(UUID.randomUUID())));
    }

    @Test
    void cancelFriendRequest_ShouldThrow_WhenNotPending() {
        UUID id = UUID.randomUUID();
        User receiver = createUser(UUID.randomUUID());

        FriendRequest request = new FriendRequest();
        request.setId(id);
        request.setReceiver(receiver);
        request.setStatus(FriendRequestStatus.ACCEPTED);

        when(friendRequestRepository.findById(id)).thenReturn(Optional.of(request));

        assertThrows(BadRequestException.class,
                () -> friendRequestService.cancelFriendRequest(id, receiver));
    }

    @Test
    void cancelFriendRequest_CurrentBehavior_AllowsOnlyReceiverToCancel() throws Exception {
        UUID id = UUID.randomUUID();
        User sender = createUser(UUID.randomUUID());
        User receiver = createUser(UUID.randomUUID());

        FriendRequest request = new FriendRequest();
        request.setId(id);
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus(FriendRequestStatus.PENDING);

        when(friendRequestRepository.findById(id)).thenReturn(Optional.of(request));
        when(friendRequestRepository.save(any(FriendRequest.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        friendRequestService.cancelFriendRequest(id, receiver);

        assertEquals(FriendRequestStatus.CANCELLED, request.getStatus());
        verify(friendRequestRepository).save(request);
    }

    @Test
    void cancelFriendRequest_CurrentBehavior_ThrowsAccessDeniedWhenSenderTriesToCancel() {
        UUID id = UUID.randomUUID();
        User sender = createUser(UUID.randomUUID());
        User receiver = createUser(UUID.randomUUID());

        FriendRequest request = new FriendRequest();
        request.setId(id);
        request.setSender(sender);
        request.setReceiver(receiver);
        request.setStatus(FriendRequestStatus.PENDING);

        when(friendRequestRepository.findById(id)).thenReturn(Optional.of(request));

        assertThrows(AccessDeniedException.class,
                () -> friendRequestService.cancelFriendRequest(id, sender));
    }
}
