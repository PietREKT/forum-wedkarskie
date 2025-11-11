package org.piet.forumbackend.fishing_spots.exceptions;

import org.piet.forumbackend.exceptions.NotFoundException;

public class FishNotFoundException extends NotFoundException {
    public FishNotFoundException(String message) {
        super(message);
    }
}
