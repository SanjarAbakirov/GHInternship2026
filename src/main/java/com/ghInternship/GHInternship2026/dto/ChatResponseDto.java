package com.ghInternship.GHInternship2026.dto;

import java.time.LocalDateTime;

/**
 * DTO ответа от AI-сервиса чата.
 *
 * @param response текст ответа нейросети
 * @param timestamp время генерации ответа
 */
public record ChatResponseDto(
        String response,
        LocalDateTime timestamp
) {
    public ChatResponseDto(String response) {
        this(response, LocalDateTime.now());
    }
}
