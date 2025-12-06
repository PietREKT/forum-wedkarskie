package org.piet.forumbackend.users.groups.dtos.requests;

import lombok.Value;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link org.piet.forumbackend.users.groups.entities.UserGroup}
 */
@Value
public class GetUserGroupDto implements Serializable {
    UUID id;
}