package com.example.demo.exception;

/**
 * Thrown when a conversation exists but does not belong to the authenticated user.
 * Maps to HTTP 403 in {@link GlobalExceptionHandler}.
 *
 * <p>Note: distinguishing this from {@link ConversationNotFoundException} (404) confirms to a
 * caller that a given conversation id exists, even if they can't access it. That's an accepted
 * trade-off here for clearer REST semantics; if stricter existence-hiding is ever required, this
 * handler can be changed to also return 404.
 */
public class UnauthorizedConversationAccessException extends RuntimeException {
    public UnauthorizedConversationAccessException(String message) {
        super(message);
    }
}
