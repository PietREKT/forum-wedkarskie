package org.piet.forumbackend.content.posts.exceptions;

import org.piet.forumbackend.exceptions.NotFoundException;

public class PostNotFoundException extends NotFoundException {
    public PostNotFoundException(String message) {
        super(message);
    }
}
