package org.piet.forumbackend.fish.dtos;

import lombok.Value;
import org.piet.forumbackend.fish.entities.Fish;
import org.piet.forumbackend.fish.entities.enums.FishingMethod;
import org.piet.forumbackend.fish.entities.enums.WaterType;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link Fish}
 */
@Value
public class GetFishDto implements Serializable {
    Long id;
    String name;
    List<FishingMethod> methods;
    WaterType waterType;
}