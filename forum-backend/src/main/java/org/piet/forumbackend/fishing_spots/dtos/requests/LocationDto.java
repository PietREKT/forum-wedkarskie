package org.piet.forumbackend.fishing_spots.dtos.requests;

import lombok.Value;

import java.io.Serializable;

@Value
public class LocationDto implements Serializable {
    Double longitude;
    Double latitude;
    AddressDto address;
}
