package com.ghInternship.GHInternship2026.exception;

/**
 * Исключение, выбрасываемое при попытке обращения к несуществующей сессии чата
 * либо к сессии, принадлежащей другому пользователю (предотвращает IDOR).
 */
public class ChatSessionNotFoundException extends RuntimeException {

    public ChatSessionNotFoundException(String message) {
        super(message);
    }

    public ChatSessionNotFoundException(Long sessionId) {
        super("Chat session with id " + sessionId + " was not found");
    }

    public ChatSessionNotFoundException(Long sessionId, String username) {
        super("Chat session with id " + sessionId + " was not found for user: " + username);
    }
}

