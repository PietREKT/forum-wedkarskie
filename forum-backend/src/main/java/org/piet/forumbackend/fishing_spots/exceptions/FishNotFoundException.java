package org.piet.forumbackend.fishing_spots.exceptions;

public class FishNotFoundException extends RuntimeException {
    public FishNotFoundException(String message) {
        super(message);
    }
}
