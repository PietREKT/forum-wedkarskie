package org.piet.forumbackend.fishing_spots.core.exceptions;

import org.piet.forumbackend.globals.exceptions.NotFoundException;

public class LocationNotFoundException extends NotFoundException {
    public LocationNotFoundException(String message) {
        super(message);
    }
}
