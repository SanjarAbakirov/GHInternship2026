package com.ghInternship.GHInternship2026.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Реализация сервиса управления и взаимодействия с внешним AI-провайдером.
 * Безопасно загружает API-ключ из конфигурации через аннотацию {@link Value},
 * предотвращая раскрытие реального секрета в открытом виде.
 */
@Service
public class ExternalAiService implements AiService {

    private static final Logger log = LoggerFactory.getLogger(ExternalAiService.class);

    private final String apiKey;

    public ExternalAiService(@Value("${ai.service.api-key:}") String apiKey) {
        this.apiKey = (apiKey != null) ? apiKey.trim() : "";
    }

    @PostConstruct
    public void init() {
        if (isConfigured()) {
            log.info("ExternalAiService успешно инициализирован. Маскированный ключ: {}", getMaskedApiKey());
        } else {
            log.warn("ExternalAiService инициализирован БЕЗ валидного API-ключа. Обращения к AI-сервису будут отклоняться.");
        }
    }

    @Override
    public boolean isConfigured() {
        return !apiKey.isBlank() && !apiKey.equalsIgnoreCase("YOUR_DEV_AI_API_KEY_HERE");
    }

    @Override
    public String getMaskedApiKey() {
        if (apiKey.isBlank()) {
            return "[НЕ ЗАДАН]";
        }
        if (apiKey.length() <= 8) {
            return "***";
        }
        int prefixLen = Math.min(4, apiKey.length() / 4);
        int suffixLen = Math.min(4, apiKey.length() / 4);
        return apiKey.substring(0, prefixLen) + "***" + apiKey.substring(apiKey.length() - suffixLen);
    }

    @Override
    public String generateResponse(String prompt) {
        if (!isConfigured()) {
            log.error("Попытка вызова AI-сервиса без сконфигурированного API-ключа");
            throw new IllegalStateException("API ключ внешнего AI-сервиса не настроен или содержит шаблонное значение.");
        }

        if (prompt == null || prompt.isBlank()) {
            throw new IllegalArgumentException("Промпт для AI-сервиса не может быть пустым.");
        }

        log.debug("Вызов внешнего AI-сервиса с промптом (длина={}) с использованием ключа {}",
                prompt.length(), getMaskedApiKey());

        // Заглушка/клиент для обращения к внешнему AI API (готово к расширению через RestClient/WebClient)
        return "AI response to prompt: '" + prompt.trim() + "'";
    }

    /**
     * Возвращает реальный API-ключ исключительно для внутренних HTTP-клиентов пакета.
     * Не должен вызываться контроллерами или отдаваться в DTO.
     *
     * @return необработанный API-ключ
     */
    String getInternalApiKey() {
        return apiKey;
    }
}

