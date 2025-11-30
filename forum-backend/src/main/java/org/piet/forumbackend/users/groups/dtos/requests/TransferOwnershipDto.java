package org.piet.forumbackend.users.groups.dtos.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.piet.forumbackend.users.core.dtos.GetUserDto;

import java.io.Serializable;
import java.util.UUID;

@Value
public class TransferOwnershipDto implements Serializable {
    @NotNull
    UUID id;
    @NotNull
    GetUserDto getUserDto;
    Boolean removeFromAdmins = false;
}