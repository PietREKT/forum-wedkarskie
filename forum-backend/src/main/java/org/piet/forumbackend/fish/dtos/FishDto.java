package org.piet.forumbackend.fish.dtos;

import lombok.Value;
import org.piet.forumbackend.fish.entities.Fish;
import org.piet.forumbackend.fish.entities.FishingMethod;
import org.piet.forumbackend.fish.entities.WaterType;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link Fish}
 */
@Value
public class FishDto implements Serializable {
    String name;
    Float avgLength;
    Float avgMass;
    String photoUrl;
    String description;
    boolean isPredatory;
    List<FishingMethod> methods;
    WaterType waterType;
}