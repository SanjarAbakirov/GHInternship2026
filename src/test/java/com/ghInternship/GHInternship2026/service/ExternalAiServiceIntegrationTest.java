package com.ghInternship.GHInternship2026.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "ai.service.api-key=test-integration-ai-key-12345"
})
class ExternalAiServiceIntegrationTest {

    @Autowired
    private AiService aiService;

    @Autowired
    private ExternalAiService externalAiService;

    @Test
    @DisplayName("Spring context should inject ExternalAiService as AiService bean")
    void shouldInjectAiServiceBean() {
        assertThat(aiService).isNotNull();
        assertThat(externalAiService).isNotNull();
        assertThat(aiService.isConfigured()).isTrue();
    }

    @Test
    @DisplayName("Should load property and mask key securely")
    void shouldLoadPropertyAndMaskKey() {
        assertThat(aiService.getMaskedApiKey()).contains("***");
        assertThat(aiService.getMaskedApiKey()).doesNotContain("test-integration-ai-key-12345");
        assertThat(externalAiService.getInternalApiKey()).isEqualTo("test-integration-ai-key-12345");
    }

    @Test
    @DisplayName("Should successfully generate response when key is present")
    void shouldGenerateResponseWhenKeyPresent() {
        String result = aiService.generateResponse("Ping");
        assertThat(result).isEqualTo("AI response to prompt: 'Ping'");
    }
}

