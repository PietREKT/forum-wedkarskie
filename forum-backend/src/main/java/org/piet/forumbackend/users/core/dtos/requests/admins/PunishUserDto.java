package org.piet.forumbackend.users.core.dtos.requests.admins;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for {@link org.piet.forumbackend.users.core.entities.User}
 */
@Value
public class PunishUserDto implements Serializable {
    @NotNull
    UUID id;
    @NotBlank
    String reason;
    Instant punishedUntil;
}