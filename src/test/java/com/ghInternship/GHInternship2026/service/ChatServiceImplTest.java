package com.ghInternship.GHInternship2026.service;

import com.ghInternship.GHInternship2026.dto.ChatRequestDto;
import com.ghInternship.GHInternship2026.dto.ChatResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private AiService aiService;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatServiceImpl(aiService);
    }

    @Test
    @DisplayName("Should process message and return AI response")
    void shouldProcessMessageSuccessfully() {
        ChatRequestDto requestDto = new ChatRequestDto("What is Spring Boot?");
        when(aiService.generateResponse("What is Spring Boot?")).thenReturn("Spring Boot is an open source Java framework.");

        ChatResponseDto responseDto = chatService.processMessage(requestDto);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.response()).isEqualTo("Spring Boot is an open source Java framework.");
        assertThat(responseDto.timestamp()).isNotNull();
        verify(aiService).generateResponse("What is Spring Boot?");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when requestDto is null")
    void shouldThrowExceptionWhenRequestDtoIsNull() {
        assertThatThrownBy(() -> chatService.processMessage(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be blank");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when message is blank")
    void shouldThrowExceptionWhenMessageIsBlank() {
        ChatRequestDto emptyDto = new ChatRequestDto("   ");
        assertThatThrownBy(() -> chatService.processMessage(emptyDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be blank");
    }
}

