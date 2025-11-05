package org.piet.forumbackend.fishing_spots.dtos;

import lombok.Value;
import org.piet.forumbackend.fishing_spots.entities.FishingMethod;
import org.piet.forumbackend.fishing_spots.entities.WaterType;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link org.piet.forumbackend.fishing_spots.entities.Fish}
 */
@Value
public class GetFishDto implements Serializable {
    Long id;
    String name;
    List<FishingMethod> methods;
    WaterType waterType;
}