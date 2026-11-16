package com.ghInternship.GHInternship2026.dto;

import com.ghInternship.GHInternship2026.entity.ChatSession;
import com.ghInternship.GHInternship2026.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ChatSessionResponseDtoTest {

    @Test
    @DisplayName("Should correctly initialize record fields with full constructor")
    void shouldInitializeFieldsCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime updated = now.plusMinutes(5);

        ChatSessionResponseDto dto = new ChatSessionResponseDto(
                42L,
                "Тестовый диалог",
                now,
                updated,
                10L,
                "Последнее сообщение"
        );

        assertThat(dto.id()).isEqualTo(42L);
        assertThat(dto.sessionId()).isEqualTo(42L);
        assertThat(dto.title()).isEqualTo("Тестовый диалог");
        assertThat(dto.createdAt()).isEqualTo(now);
        assertThat(dto.updatedAt()).isEqualTo(updated);
        assertThat(dto.messageCount()).isEqualTo(10L);
        assertThat(dto.lastMessagePreview()).isEqualTo("Последнее сообщение");
    }

    @Test
    @DisplayName("Should correctly initialize record with 4-arg constructor")
    void shouldInitializeWithFourArgConstructor() {
        LocalDateTime now = LocalDateTime.now();

        ChatSessionResponseDto dto = new ChatSessionResponseDto(
                100L,
                "Краткий диалог",
                now,
                now
        );

        assertThat(dto.id()).isEqualTo(100L);
        assertThat(dto.sessionId()).isEqualTo(100L);
        assertThat(dto.title()).isEqualTo("Краткий диалог");
        assertThat(dto.createdAt()).isEqualTo(now);
        assertThat(dto.updatedAt()).isEqualTo(now);
        assertThat(dto.messageCount()).isEqualTo(0L);
        assertThat(dto.lastMessagePreview()).isNull();
    }

    @Test
    @DisplayName("fromEntity should map JPA entity ChatSession correctly")
    void shouldMapFromEntity() {
        User user = new User("alice", "hashed");
        LocalDateTime created = LocalDateTime.of(2026, 11, 16, 12, 0);
        LocalDateTime updated = LocalDateTime.of(2026, 11, 16, 12, 10);
        ChatSession session = new ChatSession(77L, user, "Сессия пользователя", created, updated);

        ChatSessionResponseDto basicDto = ChatSessionResponseDto.fromEntity(session);
        assertThat(basicDto).isNotNull();
        assertThat(basicDto.id()).isEqualTo(77L);
        assertThat(basicDto.title()).isEqualTo("Сессия пользователя");
        assertThat(basicDto.createdAt()).isEqualTo(created);
        assertThat(basicDto.updatedAt()).isEqualTo(updated);
        assertThat(basicDto.messageCount()).isEqualTo(0L);

        ChatSessionResponseDto detailedDto = ChatSessionResponseDto.fromEntity(session, 5L, "Превью");
        assertThat(detailedDto).isNotNull();
        assertThat(detailedDto.id()).isEqualTo(77L);
        assertThat(detailedDto.messageCount()).isEqualTo(5L);
        assertThat(detailedDto.lastMessagePreview()).isEqualTo("Превью");
    }

    @Test
    @DisplayName("fromEntity should return null when session is null")
    void shouldReturnNullWhenSessionIsNull() {
        assertThat(ChatSessionResponseDto.fromEntity(null)).isNull();
        assertThat(ChatSessionResponseDto.fromEntity(null, 0L, null)).isNull();
    }
}

