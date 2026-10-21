package com.ghInternship.GHInternship2026.dto;

/**
 * DTO ответа при операциях аутентификации.
 *
 * @param message сообщение о статусе операции
 */
public record AuthResponseDto(
        String message
) {
}
