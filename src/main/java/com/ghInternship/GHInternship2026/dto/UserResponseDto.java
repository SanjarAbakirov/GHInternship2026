package com.ghInternship.GHInternship2026.dto;

/**
 * DTO ответа с данными пользователя после успешной регистрации.
 * Исключает передачу конфиденциальных полей (хэш пароля).
 *
 * @param id идентификатор пользователя
 * @param username имя пользователя
 * @param message информационное сообщение
 */
public record UserResponseDto(
        Long id,
        String username,
        String message
) {
}
