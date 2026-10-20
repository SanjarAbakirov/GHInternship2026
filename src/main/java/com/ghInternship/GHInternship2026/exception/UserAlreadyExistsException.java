package com.ghInternship.GHInternship2026.exception;

/**
 * Исключение, выбрасываемое при попытке зарегистрировать пользователя с уже занятым логином.
 * Наследуется от {@link IllegalArgumentException} для обеспечения обратной совместимости.
 */
public class UserAlreadyExistsException extends IllegalArgumentException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
