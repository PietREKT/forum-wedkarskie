package org.piet.forumbackend.content.comments.exceptions;

import org.piet.forumbackend.exceptions.NotFoundException;

public class CommentNotFoundException extends NotFoundException {
    public CommentNotFoundException(String message) {
        super(message);
    }
}
