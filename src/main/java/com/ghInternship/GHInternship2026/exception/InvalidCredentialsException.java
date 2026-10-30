package com.ghInternship.GHInternship2026.exception;

/**
 * Исключение, выбрасываемое при предоставлении неверных учетных данных пользователя
 * (неверный пароль или несуществующий логин при попытке входа в систему).
 * Перехватывается и форматируется в HTTP 401 Unauthorized через {@link GlobalExceptionHandler}.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
