package org.piet.forumbackend.fishing_spots.core.exceptions;

import org.piet.forumbackend.globals.exceptions.NotFoundException;

public class FishingSpotNotFoundException extends NotFoundException {
    public FishingSpotNotFoundException(String message) {
        super(message);
    }
}
