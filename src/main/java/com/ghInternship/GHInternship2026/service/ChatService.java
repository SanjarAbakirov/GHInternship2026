package com.ghInternship.GHInternship2026.service;

import com.ghInternship.GHInternship2026.dto.ChatRequestDto;
import com.ghInternship.GHInternship2026.dto.ChatResponseDto;

/**
 * Интерфейс сервиса чата.
 * Определяет контракт бизнес-логики обработки пользовательских сообщений
 * и взаимодействия с внешним AI-провайдером.
 */
public interface ChatService {

    /**
     * Обрабатывает входящее сообщение пользователя, делегируя вызов AI-сервису,
     * и упаковывает результат в DTO ответа.
     *
     * @param requestDto DTO с сообщением пользователя
     * @return DTO ответа с текстом от AI и временной меткой
     */
    ChatResponseDto processMessage(ChatRequestDto requestDto);
}
