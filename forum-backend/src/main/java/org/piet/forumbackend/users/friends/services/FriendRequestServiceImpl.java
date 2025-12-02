package org.piet.forumbackend.users.friends.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.piet.forumbackend.globals.exceptions.BadRequestException;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.friends.entities.FriendRequest;
import org.piet.forumbackend.users.friends.entities.FriendRequestStatus;
import org.piet.forumbackend.users.friends.repositories.FriendRequestRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class FriendRequestServiceImpl implements FriendRequestService {
    private final MessageSource messageSource;
    private final FriendRequestRepository friendRequestRepository;

    @Override
    public FriendRequest getById(UUID id) throws NotFoundException {
        return friendRequestRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage("error.users.friends.requests.not_found",
                                new Object[]{id},
                                LocaleContextHolder.getLocale()
                        )
                ));
    }

    @Override
    public FriendRequest sendFriendRequest(User from, User to) throws BadRequestException {
        if (from.equalsUser(to)) {
            throw new BadRequestException(
                    messageSource.getMessage("error.users.friends.same_user",
                            null,
                            LocaleContextHolder.getLocale()
                    )
            );
        }
        if (from.getFriends().contains(to)) {
            throw new BadRequestException(
                    messageSource.getMessage("error.users.friends.is_friend",
                            null,
                            LocaleContextHolder.getLocale()
                    )
            );
        }

        boolean exists = friendRequestRepository
                .existsBySenderAndReceiverAndStatus(from, to, FriendRequestStatus.PENDING)
                || friendRequestRepository
                .existsBySenderAndReceiverAndStatus(to, from, FriendRequestStatus.PENDING);

        if (exists) {
            throw new BadRequestException(
                    messageSource.getMessage("error.users.friends.exists",
                            null,
                            LocaleContextHolder.getLocale()
                    )
            );
        }

        FriendRequest request = new FriendRequest();
        request.setSender(from);
        request.setReceiver(to);

        log.info("User with id: {} sent friend invite to user with id: {}", from.getId(), to.getId());

        return friendRequestRepository.save(request);
    }

    @Override
    public void acceptFriendRequest(UUID requestId, User currentUser) throws NotFoundException, BadRequestException {
        FriendRequest request = getById(requestId);

        validateRequestSenderAndStatus(request, currentUser);

        User sender = request.getSender();
        User receiver = request.getReceiver();

        sender.getFriends().add(receiver);
        receiver.getFriends().add(sender);

        request.setStatus(FriendRequestStatus.ACCEPTED);
        request.setRespondedAt(Instant.now());

        log.info("User with id: {} accepted friend invite from user with id: {}", receiver.getId(), sender.getId());

        friendRequestRepository.save(request);
    }

    @Override
    public void rejectFriendRequest(UUID requestId, User currentUser) throws NotFoundException, BadRequestException {
        FriendRequest request = getById(requestId);

        validateRequestSenderAndStatus(request, currentUser);

        request.setStatus(FriendRequestStatus.REJECTED);
        request.setRespondedAt(Instant.now());

        log.info("User with id: {} rejected friend invite from user with id: {}",
                request.getReceiver().getId(),
                request.getSender().getId());

        friendRequestRepository.save(request);
    }

    @Override
    public void cancelFriendRequest(UUID requestId, User currentUser) throws NotFoundException, BadRequestException {
        FriendRequest request = getById(requestId);
        validateRequestSenderAndStatus(request, currentUser);

        request.setStatus(FriendRequestStatus.CANCELLED);

        log.info("User with id: {} cancelled friend invite to user with id: {}", currentUser.getId(), request.getReceiver().getId());

        friendRequestRepository.save(request);
    }

    private void validateRequestSenderAndStatus(FriendRequest request, User currentUser) throws BadRequestException {
        if (!request.getReceiver().equalsUser(currentUser)) {
            throw new AccessDeniedException(
                    messageSource.getMessage("error.users.friends.requests.not_receiver",
                            null,
                            LocaleContextHolder.getLocale())
            );
        }

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new BadRequestException(
                    messageSource.getMessage("error.users.friends.requests.not_pending",
                            null,
                            LocaleContextHolder.getLocale()
                    )
            );
        }
    }


}
