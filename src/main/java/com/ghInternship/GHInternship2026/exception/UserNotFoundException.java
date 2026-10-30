package com.ghInternship.GHInternship2026.exception;

/**
 * Исключение, выбрасываемое при попытке доступа к пользователю, которого нет в базе данных.
 * Перехватывается и форматируется в HTTP 404 Not Found через {@link GlobalExceptionHandler}.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
