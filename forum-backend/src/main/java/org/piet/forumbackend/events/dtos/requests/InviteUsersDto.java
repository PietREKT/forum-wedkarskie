package org.piet.forumbackend.events.dtos.requests;

import lombok.Value;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

@Value
public class InviteUsersDto implements Serializable {
    Set<UUID> userIds;
}
