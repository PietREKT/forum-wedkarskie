package org.piet.forumbackend.fishing_spots.exceptions;

import org.piet.forumbackend.exceptions.NotFoundException;

public class LocationNotFoundException extends NotFoundException {
    public LocationNotFoundException(String message) {
        super(message);
    }
}
