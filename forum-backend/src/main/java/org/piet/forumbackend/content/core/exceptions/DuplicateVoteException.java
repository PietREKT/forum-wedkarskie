package org.piet.forumbackend.content.core.exceptions;

public class DuplicateVoteException extends Exception{
    public DuplicateVoteException(String message) {
        super(message);
    }
}
