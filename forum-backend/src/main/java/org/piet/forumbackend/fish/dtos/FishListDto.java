package org.piet.forumbackend.fish.dtos;

import lombok.Value;
import org.piet.forumbackend.fish.entities.Fish;

import java.io.Serializable;

/**
 * DTO for {@link org.piet.forumbackend.fish.entities.Fish}
 */
@Value
public class FishListDto implements Serializable {
    Long id;
    String name;
    String photoUrl;

    public static FishListDto create(Fish fish){
        return new FishListDto(
                fish.getId(),
                fish.getName(),
                fish.getPhotoUrl()
        );
    }
}