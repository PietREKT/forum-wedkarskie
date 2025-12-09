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
public class FishDto implements Serializable {
    Long id;
    String name;
    Float avgLength;
    Float avgMass;
    String photoUrl;
    String description;
    boolean isPredatory;
    List<FishingMethod> methods;
    WaterType waterType;

    public static FishDto create(Fish fish){
        return new FishDto(
                fish.getId(),
                fish.getName(),
                fish.getAvgLength(),
                fish.getAvgMass(),
                fish.getPhotoUrl(),
                fish.getDescription(),
                fish.isPredatory(),
                fish.getMethods(),
                fish.getWaterType()
        );
    }
}