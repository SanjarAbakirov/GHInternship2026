package com.example.demo.exception;

/**
 * Thrown when a requested conversation id does not exist at all, regardless of owner.
 * Maps to HTTP 404 in {@link GlobalExceptionHandler}.
 */
public class ConversationNotFoundException extends RuntimeException {
    public ConversationNotFoundException(String message) {
        super(message);
    }
}
