package org.piet.forumbackend.fishing_spots.dtos;

import lombok.Value;
import org.piet.forumbackend.fishing_spots.entities.FishingMethod;
import org.piet.forumbackend.fishing_spots.entities.WaterType;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link org.piet.forumbackend.fishing_spots.entities.Fish}
 */
@Value
public class CreateFishDto implements Serializable {
    String name;
    Float avgLengthCm;
    Float avgMassKg;
    String description;
    MultipartFile photo;
    boolean isPredatory;
    List<FishingMethod> methods;
    WaterType waterType;
}