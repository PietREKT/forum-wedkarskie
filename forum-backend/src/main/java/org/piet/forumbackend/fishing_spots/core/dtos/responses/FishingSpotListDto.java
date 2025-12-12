package org.piet.forumbackend.fishing_spots.core.dtos.responses;

import lombok.Value;
import org.piet.forumbackend.fishing_spots.core.entities.FishingSpot;

import java.io.Serializable;

/**
 * DTO for {@link FishingSpot}
 */
@Value
public class FishingSpotListDto implements Serializable {
    Long id;
    String name;
    double locationX, locationY;
    FishingSpot.FISHING_SPOT_TYPE type;
    double avgRating;

    public static FishingSpotListDto create(FishingSpot spot){
        return new FishingSpotListDto(
                spot.getId(),
                spot.getName(),
                spot.getLocation().getX(),
                spot.getLocation().getY(),
                spot.getType(),
                spot.getAverageRating()
        );
    }
}