package org.piet.forumbackend.users.friends.dtos.requests;

import lombok.Value;
import org.piet.forumbackend.users.core.dtos.requests.GetUserDto;

import java.io.Serializable;

/**
 * DTO for {@link org.piet.forumbackend.users.friends.entities.FriendRequest}
 */
@Value
public class FriendRequestDto implements Serializable {
    GetUserDto receiver;
}