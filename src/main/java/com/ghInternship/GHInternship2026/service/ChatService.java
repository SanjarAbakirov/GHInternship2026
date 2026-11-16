package com.ghInternship.GHInternship2026.service;

import com.ghInternship.GHInternship2026.dto.ChatRequestDto;
import com.ghInternship.GHInternship2026.dto.ChatResponseDto;

/**
 * Интерфейс сервиса чата.
 * Определяет контракт бизнес-логики обработки пользовательских сообщений,
 * персистентности сессий диалогов и взаимодействия с внешним AI-провайдером.
 */
public interface ChatService {

    /**
     * Обрабатывает сообщение пользователя, определяя или создавая сессию диалога,
     * сохраняя реплику пользователя и ответ AI в базу данных.
     *
     * @param requestDto DTO с сообщением пользователя и опциональным ID сессии
     * @param username имя авторизованного пользователя (владельца сессии)
     * @return DTO ответа с ID сессии, текстом от AI и временной меткой
     */
    ChatResponseDto processMessage(ChatRequestDto requestDto, String username);

    /**
     * Обрабатывает входящее сообщение пользователя (перегруженный метод для обратной
     * совместимости, определяющий пользователя из контекста безопасности).
     *
     * @param requestDto DTO с сообщением пользователя
     * @return DTO ответа с текстом от AI и временной меткой
     */
    default ChatResponseDto processMessage(ChatRequestDto requestDto) {
        return processMessage(requestDto, null);
    }
}
