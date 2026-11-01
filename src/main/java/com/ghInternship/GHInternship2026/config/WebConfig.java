package com.ghInternship.GHInternship2026.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Конфигурация Spring Web MVC.
 * Определяет глобальную конфигурацию CORS (Cross-Origin Resource Sharing)
 * через бин {@link WebMvcConfigurer} для разрешения запросов от фронтенда ReactJS.
 */
@Configuration
public class WebConfig {

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

    @Value("#{'${app.cors.allowed-origins:http://localhost:3000,http://127.0.0.1:3000,http://localhost:5173,http://127.0.0.1:5173}'.split(',')}")
    private List<String> allowedOrigins;

    @Value("#{'${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS,PATCH,HEAD}'.split(',')}")
    private List<String> allowedMethods;

    @Value("#{'${app.cors.allowed-headers:*}'.split(',')}")
    private List<String> allowedHeaders;

    @Value("${app.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${app.cors.max-age:3600}")
    private long maxAge;

    /**
     * Бин {@link WebMvcConfigurer} для глобальной настройки правил CORS в Spring MVC.
     * Разрешает запросы от dev-серверов ReactJS (порты 3000 и 5173) и поддерживает
     * безопасное переопределение разрешенных origins для production через application.properties.
     *
     * @return настроенный экземпляр {@link WebMvcConfigurer}
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                log.info("Регистрация глобальной CORS конфигурации WebMvcConfigurer: origins={}, methods={}, credentials={}, maxAge={}",
                        allowedOrigins, allowedMethods, allowCredentials, maxAge);

                registry.addMapping("/**")
                        .allowedOrigins(allowedOrigins.toArray(String[]::new))
                        .allowedMethods(allowedMethods.toArray(String[]::new))
                        .allowedHeaders(allowedHeaders.toArray(String[]::new))
                        .allowCredentials(allowCredentials)
                        .maxAge(maxAge);
            }
        };
    }

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public List<String> getAllowedMethods() {
        return allowedMethods;
    }

    public List<String> getAllowedHeaders() {
        return allowedHeaders;
    }

    public boolean isAllowCredentials() {
        return allowCredentials;
    }

    public long getMaxAge() {
        return maxAge;
    }
}

