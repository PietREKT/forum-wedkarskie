package org.piet.forumbackend.content.exceptions;

public class DuplicateVoteException extends Exception{
    public DuplicateVoteException(String message) {
        super(message);
    }
}
