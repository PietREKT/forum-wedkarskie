package org.piet.forumbackend.fishing_spots.exceptions;

import org.piet.forumbackend.exceptions.NotFoundException;

public class FishingSpotNotFoundException extends NotFoundException {
    public FishingSpotNotFoundException(String message) {
        super(message);
    }
}
