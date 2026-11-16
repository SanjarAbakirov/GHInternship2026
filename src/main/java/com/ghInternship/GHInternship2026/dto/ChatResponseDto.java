package com.ghInternship.GHInternship2026.dto;

import java.time.LocalDateTime;

/**
 * DTO ответа от AI-сервиса чата.
 *
 * @param chatSessionId идентификатор сессии чата (созданной или существующей)
 * @param response текст ответа нейросети
 * @param timestamp время генерации ответа
 */
public record ChatResponseDto(
        Long chatSessionId,
        String response,
        LocalDateTime timestamp
) {
    public ChatResponseDto(String response) {
        this(null, response, LocalDateTime.now());
    }

    public ChatResponseDto(String response, LocalDateTime timestamp) {
        this(null, response, timestamp);
    }

    public ChatResponseDto(Long chatSessionId, String response) {
        this(chatSessionId, response, LocalDateTime.now());
    }

    /**
     * Алиас для {@link #chatSessionId()}.
     */
    public Long sessionId() {
        return chatSessionId;
    }
}
