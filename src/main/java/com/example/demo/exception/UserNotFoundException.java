package com.example.demo.exception;

/**
 * Thrown when an authenticated JWT resolves to a username that no longer has a corresponding
 * {@link com.example.demo.model.User} row (e.g. the account was deleted after the token was
 * issued). Maps to HTTP 404 in {@link GlobalExceptionHandler}.
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
