package com.ghInternship.GHInternship2026.dto;

import com.ghInternship.GHInternship2026.entity.ChatMessage;
import com.ghInternship.GHInternship2026.entity.ChatSession;
import com.ghInternship.GHInternship2026.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ChatMessageResponseDtoTest {

    @Test
    @DisplayName("Should correctly initialize record fields and aliases")
    void shouldInitializeFieldsAndAliases() {
        LocalDateTime now = LocalDateTime.now();

        ChatMessageResponseDto dto = new ChatMessageResponseDto(
                55L,
                10L,
                "user",
                "Привет, AI!",
                now
        );

        assertThat(dto.id()).isEqualTo(55L);
        assertThat(dto.sessionId()).isEqualTo(10L);
        assertThat(dto.senderRole()).isEqualTo("user");
        assertThat(dto.role()).isEqualTo("user");
        assertThat(dto.sender()).isEqualTo("user");
        assertThat(dto.content()).isEqualTo("Привет, AI!");
        assertThat(dto.message()).isEqualTo("Привет, AI!");
        assertThat(dto.text()).isEqualTo("Привет, AI!");
        assertThat(dto.timestamp()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should correctly initialize with 4-arg constructor")
    void shouldInitializeWithFourArgConstructor() {
        LocalDateTime now = LocalDateTime.now();

        ChatMessageResponseDto dto = new ChatMessageResponseDto(
                12L,
                "ai",
                "Здравствуйте!",
                now
        );

        assertThat(dto.id()).isEqualTo(12L);
        assertThat(dto.sessionId()).isNull();
        assertThat(dto.senderRole()).isEqualTo("ai");
        assertThat(dto.content()).isEqualTo("Здравствуйте!");
        assertThat(dto.timestamp()).isEqualTo(now);
    }

    @Test
    @DisplayName("fromEntity should map JPA entity ChatMessage correctly")
    void shouldMapFromEntity() {
        User user = new User("bob", "hash");
        ChatSession session = new ChatSession(101L, user, "Тест сессии", LocalDateTime.now());
        ChatMessage message = ChatMessage.userMessage(session, "Как работает Spring Boot?");

        ChatMessageResponseDto dto = ChatMessageResponseDto.fromEntity(message);
        assertThat(dto).isNotNull();
        assertThat(dto.sessionId()).isEqualTo(101L);
        assertThat(dto.senderRole()).isEqualTo("user");
        assertThat(dto.content()).isEqualTo("Как работает Spring Boot?");
        assertThat(dto.timestamp()).isEqualTo(message.getTimestamp());
    }

    @Test
    @DisplayName("fromEntity should return null when message is null")
    void shouldReturnNullWhenMessageIsNull() {
        assertThat(ChatMessageResponseDto.fromEntity(null)).isNull();
    }
}

