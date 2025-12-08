package org.piet.forumbackend.users.groups.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.piet.forumbackend.users.core.dtos.requests.GetUserDto;

import java.io.Serializable;

@Value
public class TransferOwnershipDto implements Serializable {
    @NotNull
    GetUserDto getUserDto;
    Boolean removeFromAdmins = false;
}