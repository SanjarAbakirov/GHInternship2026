package com.ghInternship.GHInternship2026.service;

import com.ghInternship.GHInternship2026.dto.ChatRequestDto;
import com.ghInternship.GHInternship2026.dto.ChatResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Реализация интерфейса {@link ChatService}.
 * Инкапсулирует бизнес-логику чата и делегирует непосредственное обращение к LLM сервису {@link AiService}.
 */
@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final AiService aiService;

    public ChatServiceImpl(AiService aiService) {
        this.aiService = Objects.requireNonNull(aiService, "aiService must not be null");
    }

    @Override
    public ChatResponseDto processMessage(ChatRequestDto requestDto) {
        if (requestDto == null || requestDto.message() == null || requestDto.message().isBlank()) {
            log.warn("Попытка обработки пустого сообщения в ChatService");
            throw new IllegalArgumentException("Chat message must not be blank");
        }

        String userMessage = requestDto.message().trim();
        log.info("Обработка сообщения пользователя в ChatService (длина: {} символов)", userMessage.length());

        String aiResponse = aiService.generateResponse(userMessage);
        log.debug("Получен ответ от AI-сервиса (длина: {} символов)", aiResponse != null ? aiResponse.length() : 0);

        return new ChatResponseDto(aiResponse);
    }
}

