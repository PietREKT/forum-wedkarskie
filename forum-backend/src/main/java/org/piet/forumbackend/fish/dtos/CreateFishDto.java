package org.piet.forumbackend.fish.dtos;

import lombok.Value;
import org.piet.forumbackend.fish.entities.Fish;
import org.piet.forumbackend.fish.entities.enums.FishingMethod;
import org.piet.forumbackend.fish.entities.enums.WaterType;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link Fish}
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