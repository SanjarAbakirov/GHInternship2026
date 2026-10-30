package com.ghInternship.GHInternship2026.exception;

import org.springframework.validation.BindingResult;

/**
 * Исключение, выбрасываемое при обнаружении ошибок валидации входных данных запроса.
 * Инкапсулирует {@link BindingResult} с деталями ошибок каждого поля.
 * Перехватывается и форматируется в HTTP 400 Bad Request через {@link GlobalExceptionHandler}.
 */
public class ValidationException extends RuntimeException {

    private final BindingResult bindingResult;

    public ValidationException(BindingResult bindingResult) {
        super("Validation failed for one or more fields");
        this.bindingResult = bindingResult;
    }

    public ValidationException(String message, BindingResult bindingResult) {
        super(message);
        this.bindingResult = bindingResult;
    }

    public BindingResult getBindingResult() {
        return bindingResult;
    }
}
