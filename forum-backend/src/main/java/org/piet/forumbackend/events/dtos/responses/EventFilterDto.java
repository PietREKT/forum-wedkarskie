package org.piet.forumbackend.events.dtos.responses;

import lombok.Value;

import java.io.Serializable;
import java.time.Instant;

@Value
public class EventFilterDto implements Serializable {
    Long fishingSpotId;
    Long groupId;
    Instant from;
    Instant to;
}
