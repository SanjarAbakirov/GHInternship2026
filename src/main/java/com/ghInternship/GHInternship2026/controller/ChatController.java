package com.ghInternship.GHInternship2026.controller;

import com.ghInternship.GHInternship2026.dto.ChatRequestDto;
import com.ghInternship.GHInternship2026.dto.ChatResponseDto;
import com.ghInternship.GHInternship2026.service.ChatService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * REST-контроллер для обработки запросов к AI-чату.
 * Спроектирован как тонкий контроллер (Thin Controller): отвечает исключительно
 * за прием HTTP-запроса, валидацию входного DTO, передачу контекста аутентификации
 * и делегирование логики диалогов в {@link ChatService}.
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = Objects.requireNonNull(chatService, "chatService must not be null");
    }

    /**
     * Эндпоинт отправки сообщения пользователя в AI-чат с сохранением сессии и сообщений.
     *
     * @param requestDto DTO сообщения пользователя с опциональным chatSessionId
     * @param authentication объект текущей аутентификации пользователя
     * @return HTTP 200 OK с DTO ответа от AI и ID сессии диалога
     */
    @PostMapping
    public ResponseEntity<ChatResponseDto> sendMessage(
            @Valid @RequestBody ChatRequestDto requestDto,
            Authentication authentication
    ) {
        String username = (authentication != null) ? authentication.getName() : null;
        log.info("Получен HTTP POST запрос на /api/chat от пользователя: {}", username);
        ChatResponseDto responseDto = chatService.processMessage(requestDto, username);
        return ResponseEntity.ok(responseDto);
    }
}
