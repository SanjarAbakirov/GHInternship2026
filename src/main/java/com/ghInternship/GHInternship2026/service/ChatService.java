package com.ghInternship.GHInternship2026.service;

import com.ghInternship.GHInternship2026.dto.ChatMessageResponseDto;
import com.ghInternship.GHInternship2026.dto.ChatRequestDto;
import com.ghInternship.GHInternship2026.dto.ChatResponseDto;
import com.ghInternship.GHInternship2026.dto.ChatSessionResponseDto;

import java.util.List;

/**
 * Интерфейс сервиса чата.
 * Определяет контракт бизнес-логики обработки пользовательских сообщений,
 * персистентности сессий диалогов и взаимодействия с внешним AI-провайдером,
 * а также извлечения истории диалогов и сообщений.
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

    /**
     * Получить список всех сессий диалогов для аутентифицированного пользователя.
     *
     * @param username имя авторизованного пользователя
     * @return список DTO сессий диалогов, отсортированных по наиболее недавней активности
     */
    List<ChatSessionResponseDto> getUserChatSessions(String username);

    /**
     * Получить все сообщения конкретной сессии диалога с обязательной проверкой прав доступа.
     *
     * @param sessionId идентификатор сессии диалога
     * @param username имя авторизованного пользователя
     * @return список DTO сообщений сессии в хронологическом порядке
     */
    List<ChatMessageResponseDto> getSessionMessages(Long sessionId, String username);
}
