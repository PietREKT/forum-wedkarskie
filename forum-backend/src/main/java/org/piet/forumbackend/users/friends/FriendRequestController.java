package org.piet.forumbackend.users.friends;

import org.piet.forumbackend.globals.exceptions.BadRequestException;
import org.piet.forumbackend.globals.exceptions.NotFoundException;
import org.piet.forumbackend.users.core.dtos.requests.GetUserDto;
import org.piet.forumbackend.users.core.entities.User;
import org.piet.forumbackend.users.core.exceptions.UserNotLoggedInException;
import org.piet.forumbackend.users.core.services.UserService;
import org.piet.forumbackend.users.friends.services.FriendRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("${forum.api.prefix}/users/friends")
public class FriendRequestController {
    private final UserService userService;
    private final FriendRequestService friendRequestService;

    public FriendRequestController(UserService userService, FriendRequestService friendRequestService) {
        this.userService = userService;
        this.friendRequestService = friendRequestService;
    }

    @PostMapping("/invite")
    public ResponseEntity<?> sendFriendInvite(@RequestBody GetUserDto target, Authentication authentication) throws UserNotLoggedInException, NotFoundException, BadRequestException {
        User sender = userService.getUserFromAuth(authentication);
        User receiver = userService.getUserById(target.getId());

        friendRequestService.sendFriendRequest(sender, receiver);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{reqId}/accept")
    public ResponseEntity<?> acceptFriendInvite(@PathVariable UUID reqId, Authentication authentication) throws NotFoundException, BadRequestException, UserNotLoggedInException {
        User currentUser = userService.getUserFromAuth(authentication);
        friendRequestService.acceptFriendRequest(reqId, currentUser);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{reqId}/reject")
    public ResponseEntity<?> rejectFriendInvite(@PathVariable UUID reqId, Authentication authentication) throws UserNotLoggedInException, NotFoundException, BadRequestException {
        User currentUser = userService.getUserFromAuth(authentication);
        friendRequestService.rejectFriendRequest(reqId, currentUser);

        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{reqId}/cancel")
    public ResponseEntity<?> cancelFriendInvite(@PathVariable UUID reqId, Authentication authentication) throws UserNotLoggedInException, NotFoundException, BadRequestException {
        User currentUser = userService.getUserFromAuth(authentication);
        friendRequestService.cancelFriendRequest(reqId, currentUser);

        return ResponseEntity.ok().build();
    }
}
