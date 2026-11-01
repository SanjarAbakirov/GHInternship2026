package com.ghInternship.GHInternship2026.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Интеграционные тесты для глобальной конфигурации CORS (Cross-Origin Resource Sharing).
 * Проверяют корректность работы {@link WebConfig}, {@link WebMvcConfigurer} и интеграцию со Spring Security.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CorsConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebConfig webConfig;

    @Autowired
    private WebMvcConfigurer corsConfigurer;

    @Test
    @DisplayName("WebMvcConfigurer bean should be present in ApplicationContext")
    void webMvcConfigurerBeanShouldBePresent() {
        assertThat(webConfig).isNotNull();
        assertThat(corsConfigurer).isNotNull();
        assertThat(webConfig.getAllowedOrigins())
                .contains("http://localhost:3000", "http://localhost:5173", "http://127.0.0.1:3000", "http://127.0.0.1:5173");
        assertThat(webConfig.isAllowCredentials()).isTrue();
        assertThat(webConfig.getMaxAge()).isEqualTo(3600);
    }

    @Test
    @DisplayName("OPTIONS preflight from React dev server (http://localhost:3000) should be permitted with valid CORS headers")
    void shouldAllowPreflightFromReactPort3000() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type,Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(header().string("Access-Control-Max-Age", "3600"))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("POST")));
    }

    @Test
    @DisplayName("OPTIONS preflight from React Vite server (http://localhost:5173) should be permitted")
    void shouldAllowPreflightFromReactVitePort5173() throws Exception {
        mockMvc.perform(options("/api/auth/register")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    @DisplayName("GET /hello with Origin: http://localhost:3000 should include Access-Control-Allow-Origin header")
    void shouldIncludeCorsHeaderOnActualGetRequest() throws Exception {
        mockMvc.perform(get("/hello")
                        .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    @DisplayName("OPTIONS preflight from untrusted origin (e.g. http://malicious-site.com) should NOT return allow origin")
    void shouldRejectCorsPreflightFromUntrustedOrigin() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://malicious-site.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
}

