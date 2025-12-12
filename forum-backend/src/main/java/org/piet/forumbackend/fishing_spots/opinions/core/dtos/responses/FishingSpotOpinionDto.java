package org.piet.forumbackend.fishing_spots.opinions.core.dtos.responses;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Value;
import org.piet.forumbackend.fishing_spots.opinions.core.entities.FishingSpotOpinion;
import org.piet.forumbackend.users.core.dtos.responses.ListUserDto;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for {@link FishingSpotOpinion}
 */
@Value
public class FishingSpotOpinionDto implements Serializable {
    Long id;
    @Min(1)
    @Max(5)
    Integer rating;
    String comment;
    ListUserDto author;
    Instant createdAt;
}