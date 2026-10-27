package com.ghInternship.GHInternship2026.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO для передачи учетных данных при аутентификации (входе).
 *
 * @param username имя пользователя
 * @param password пароль
 */
public record UserLoginDto(
        @NotNull(message = "Username must not be null")
        @NotBlank(message = "Username must not be blank")
        String username,

        @NotNull(message = "Password must not be null")
        @NotBlank(message = "Password must not be blank")
        String password
) {
}
