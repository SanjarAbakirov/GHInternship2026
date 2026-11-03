package com.ghInternship.GHInternship2026.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO (Data Transfer Object) для передачи сообщения пользователя в AI-чат.
 *
 * @param message текст сообщения от пользователя (обязательное, от 1 до 2000 символов)
 */
public record ChatRequestDto(
        @NotNull(message = "Message must not be null")
        @NotBlank(message = "Message must not be blank")
        @Size(min = 1, max = 2000, message = "Message must be between 1 and 2000 characters")
        String message
) {
}

