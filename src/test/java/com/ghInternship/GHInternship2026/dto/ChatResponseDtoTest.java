package com.ghInternship.GHInternship2026.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ChatResponseDtoTest {

    @Test
    @DisplayName("Single-argument constructor should automatically set timestamp")
    void singleArgConstructorShouldSetTimestamp() {
        ChatResponseDto dto = new ChatResponseDto("AI reply text");
        assertThat(dto.response()).isEqualTo("AI reply text");
        assertThat(dto.timestamp()).isNotNull();
        assertThat(dto.timestamp()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("Explicit constructor should preserve custom timestamp")
    void explicitConstructorShouldPreserveTimestamp() {
        LocalDateTime customTime = LocalDateTime.of(2026, 11, 3, 12, 0);
        ChatResponseDto dto = new ChatResponseDto("Custom reply", customTime);
        assertThat(dto.response()).isEqualTo("Custom reply");
        assertThat(dto.timestamp()).isEqualTo(customTime);
        assertThat(dto.chatSessionId()).isNull();
    }

    @Test
    @DisplayName("Should support chatSessionId in constructor and alias sessionId()")
    void shouldSupportChatSessionId() {
        LocalDateTime time = LocalDateTime.now();
        ChatResponseDto dto = new ChatResponseDto(99L, "Response with session", time);
        assertThat(dto.chatSessionId()).isEqualTo(99L);
        assertThat(dto.sessionId()).isEqualTo(99L);
        assertThat(dto.response()).isEqualTo("Response with session");
        assertThat(dto.timestamp()).isEqualTo(time);

        ChatResponseDto dtoWithoutTime = new ChatResponseDto(100L, "Auto timestamp");
        assertThat(dtoWithoutTime.chatSessionId()).isEqualTo(100L);
        assertThat(dtoWithoutTime.timestamp()).isNotNull();
    }
}

