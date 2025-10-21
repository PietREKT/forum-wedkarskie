package org.piet.forumbackend.utils;

import org.piet.forumbackend.posts.exceptions.PostNotFoundException;
import org.piet.forumbackend.users.UserNotLoggedInException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({BadCredentialsException.class, UserNotLoggedInException.class})
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException e){
        Map<String, String> m = new HashMap<>();
        m.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(m);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, String>> handleIoException(IOException e){
        Map<String, String> m = new HashMap<>();
        m.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(m);
    }

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<Map<String, String>> handlePostNotFound(PostNotFoundException e){
        Map<String, String> m = new HashMap<>();
        m.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(m);
    }
}
