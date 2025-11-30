package org.piet.forumbackend.users.groups.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.piet.forumbackend.users.core.dtos.GetUserDto;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link org.piet.forumbackend.users.groups.entities.UserGroup}
 */
@Value
public class ModifyMemberUserGroupDto implements Serializable {
    @NotNull
    UUID id;
    @NotNull
    GetUserDto getUserDto;
}