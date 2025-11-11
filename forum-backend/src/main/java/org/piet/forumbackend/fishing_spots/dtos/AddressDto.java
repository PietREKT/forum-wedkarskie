package org.piet.forumbackend.fishing_spots.dtos;

import lombok.Value;

import java.io.Serializable;

@Value
public class AddressDto implements Serializable {
    String countryCode;
    String municipality;
    String city;
    String street;
    Long propertyNo;

    @Override
    public String toString() {
        return "AddressDto{" +
                "countryCode='" + countryCode + '\'' +
                ", municipality='" + municipality + '\'' +
                ", city='" + city + '\'' +
                ", street='" + street + '\'' +
                ", propertyNo=" + propertyNo +
                '}';
    }
}
