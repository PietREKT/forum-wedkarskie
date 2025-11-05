package org.piet.forumbackend.exceptions;

public class BadRequestException extends Exception{
    public BadRequestException(String message) {
        super(message);
    }
}
