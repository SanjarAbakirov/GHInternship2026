package com.ghInternship.GHInternship2026.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO для стандартизированного ответа об ошибке REST API.
 *
 * @param timestamp время возникновения ошибки
 * @param status числовой HTTP статус код
 * @param error текстовое обозначение HTTP статуса
 * @param message описание ошибки
 * @param path URI запроса
 * @param validationErrors детальные ошибки валидации полей (если применимо)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponseDto(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> validationErrors
) {
    public ErrorResponseDto(int status, String error, String message, String path) {
        this(LocalDateTime.now(), status, error, message, path, null);
    }

    public ErrorResponseDto(int status, String error, String message, String path, Map<String, String> validationErrors) {
        this(LocalDateTime.now(), status, error, message, path, validationErrors);
    }
}
