package com.ghInternship.GHInternship2026.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExternalAiServiceTest {

    @Test
    @DisplayName("Should correctly configure service when valid API key is provided")
    void shouldBeConfiguredWhenValidKeyProvided() {
        ExternalAiService service = new ExternalAiService("sk-1234567890abcdef");

        assertThat(service.isConfigured()).isTrue();
        assertThat(service.getMaskedApiKey()).isEqualTo("sk-1***cdef");
        assertThat(service.generateResponse("Hello AI")).contains("Hello AI");
    }

    @Test
    @DisplayName("Should detect unconfigured state when API key is empty or null")
    void shouldDetectUnconfiguredStateWhenKeyIsEmpty() {
        ExternalAiService serviceWithNull = new ExternalAiService(null);
        assertThat(serviceWithNull.isConfigured()).isFalse();
        assertThat(serviceWithNull.getMaskedApiKey()).isEqualTo("[НЕ ЗАДАН]");

        ExternalAiService serviceWithEmpty = new ExternalAiService("   ");
        assertThat(serviceWithEmpty.isConfigured()).isFalse();
        assertThat(serviceWithEmpty.getMaskedApiKey()).isEqualTo("[НЕ ЗАДАН]");

        assertThatThrownBy(() -> serviceWithEmpty.generateResponse("test"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("не настроен");
    }

    @Test
    @DisplayName("Should detect placeholder template key as unconfigured")
    void shouldDetectPlaceholderTemplateKeyAsUnconfigured() {
        ExternalAiService service = new ExternalAiService("YOUR_DEV_AI_API_KEY_HERE");

        assertThat(service.isConfigured()).isFalse();
        assertThatThrownBy(() -> service.generateResponse("Hello"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("не настроен или содержит шаблонное значение");
    }

    @Test
    @DisplayName("Should throw exception when prompt is blank")
    void shouldThrowExceptionWhenPromptIsBlank() {
        ExternalAiService service = new ExternalAiService("valid-secret-key-12345");

        assertThatThrownBy(() -> service.generateResponse("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("не может быть пустым");
    }

    @Test
    @DisplayName("Masked key should never expose the full secret")
    void maskedKeyShouldNeverExposeFullSecret() {
        String secret = "secret-super-confidential-token-999";
        ExternalAiService service = new ExternalAiService(secret);

        String masked = service.getMaskedApiKey();
        assertThat(masked).contains("***");
        assertThat(masked).isNotEqualTo(secret);
        assertThat(masked.length()).isLessThan(secret.length());
    }
}

