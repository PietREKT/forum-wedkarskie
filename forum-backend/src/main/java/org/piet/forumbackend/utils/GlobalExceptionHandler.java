package org.piet.forumbackend.utils;

import org.piet.forumbackend.content.comments.exceptions.CommentNotFoundException;
import org.piet.forumbackend.content.posts.exceptions.PostNotFoundException;
import org.piet.forumbackend.content.reports.exceptions.ContentReportNotFoundException;
import org.piet.forumbackend.users.exceptions.UserNotLoggedInException;
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
    public ResponseEntity<Map<String, String>> handleBadCredentials(Exception e) {
        Map<String, String> m = new HashMap<>();
        m.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(m);
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, String>> handleIoException(IOException e) {
        Map<String, String> m = new HashMap<>();
        m.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(m);
    }

    @ExceptionHandler({PostNotFoundException.class, CommentNotFoundException.class, ContentReportNotFoundException.class})
    public ResponseEntity<Map<String, String>> handlePostNotFound(Exception e) {
        Map<String, String> m = new HashMap<>();
        m.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(m);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        Map<String, String> m = new HashMap<>();
        m.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(m);
    }
}
