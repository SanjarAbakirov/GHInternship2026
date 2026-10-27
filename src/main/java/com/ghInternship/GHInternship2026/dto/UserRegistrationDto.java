package com.ghInternship.GHInternship2026.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO (Data Transfer Object) для передачи данных при регистрации нового пользователя.
 *
 * @param username имя пользователя (логин), обязательное, от 3 до 50 символов
 * @param password пароль в открытом виде (raw), обязательный, от 6 до 100 символов
 */
public record UserRegistrationDto(
        @NotNull(message = "Username must not be null")
        @NotBlank(message = "Username must not be blank")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @NotNull(message = "Password must not be null")
        @NotBlank(message = "Password must not be blank")
        @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
        String password
) {
}
