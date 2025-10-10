package org.piet.forumbackend.fishing_spots.dtos;

import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link org.piet.forumbackend.fishing_spots.entities.Fish}
 */
@Value
public class FishDto implements Serializable {
    Long id;
    String name;
    String species;
    Float avgLength;
    Float avgMass;
}