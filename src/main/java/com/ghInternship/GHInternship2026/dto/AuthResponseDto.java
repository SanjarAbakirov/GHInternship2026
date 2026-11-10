package com.ghInternship.GHInternship2026.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO ответа при операциях аутентификации.
 *
 * @param message  сообщение о статусе операции
 * @param token    JWT-токен для доступа к защищенным эндпоинтам
 * @param username имя аутентифицированного пользователя
 * @param id       идентификатор пользователя
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponseDto(
        String message,
        String token,
        String username,
        Long id
) {
    /**
     * Конструктор обратной совместимости (только с сообщением).
     */
    public AuthResponseDto(String message) {
        this(message, null, null, null);
    }
}
