package org.piet.forumbackend.utils;

import lombok.RequiredArgsConstructor;
import org.piet.forumbackend.exceptions.BadRequestException;
import org.piet.forumbackend.exceptions.NotFoundException;
import org.piet.forumbackend.users.exceptions.UserNotLoggedInException;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private final MessageSource messageSource;

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

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handlePostNotFound(Exception e) {
        Map<String, String> m = new HashMap<>();
        m.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(m);
    }

    //Specialized 500
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        Map<String, String> m = new HashMap<>();
        m.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(m);
    }

    //Generic 500
    @ExceptionHandler(NoSuchMessageException.class)
    public ResponseEntity<Map<String, String>> handleNoSuchMessage(NoSuchMessageException e) {
        Map<String, String> m = Map.of(
        "message",
                Objects.requireNonNull(messageSource.getMessage("error.internal_server_error",
                        null,
                        "Something went wrong. Please try again later.",
                        LocaleContextHolder.getLocale())));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(m);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(BadRequestException e){
        Map<String, String> m = Map.of(
                "message", e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(m);
    }
}
