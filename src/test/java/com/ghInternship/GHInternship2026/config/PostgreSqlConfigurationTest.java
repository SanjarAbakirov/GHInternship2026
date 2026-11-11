package com.ghInternship.GHInternship2026.config;

import org.hibernate.dialect.PostgreSQLDialect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Driver;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты конфигурации драйвера PostgreSQL и безопасности шаблона настроек БД.
 * Проверяют совместимость версий JDBC-драйвера PostgreSQL, диалекта Hibernate
 * и отсутствие захардкоженных паролей в версионируемых конфигурационных файлах.
 */
class PostgreSqlConfigurationTest {

    @Test
    @DisplayName("PostgreSQL JDBC Driver should be available on classpath and compatible (version >= 42)")
    void postgresqlDriverShouldBePresentAndCompatible() throws Exception {
        Class<?> driverClass = Class.forName("org.postgresql.Driver");
        Driver driver = (Driver) driverClass.getDeclaredConstructor().newInstance();

        assertThat(driver).isNotNull();
        assertThat(driver.getMajorVersion()).isGreaterThanOrEqualTo(42);
        assertThat(driver.acceptsURL("jdbc:postgresql://localhost:5432/gh_internship_db")).isTrue();
    }

    @Test
    @DisplayName("Hibernate PostgreSQLDialect should be available on classpath for JPA ORM mapping")
    void hibernatePostgreSqlDialectShouldBePresent() {
        PostgreSQLDialect dialect = new PostgreSQLDialect();
        assertThat(dialect).isNotNull();
    }

    @Test
    @DisplayName("application.properties.example should configure PostgreSQL securely via env vars without real passwords")
    void examplePropertiesShouldUsePostgreSqlAndEnvPlaceholders() throws Exception {
        Path examplePath = Path.of("src/main/resources/application.properties.example");
        assertThat(Files.exists(examplePath)).isTrue();

        Properties props = new Properties();
        try (var reader = Files.newBufferedReader(examplePath)) {
            props.load(reader);
        }

        assertThat(props.getProperty("spring.datasource.driver-class-name"))
                .isEqualTo("org.postgresql.Driver");
        assertThat(props.getProperty("spring.datasource.url"))
                .startsWith("${DB_URL:jdbc:postgresql://");
        assertThat(props.getProperty("spring.datasource.username"))
                .startsWith("${DB_USERNAME:");
        assertThat(props.getProperty("spring.datasource.password"))
                .startsWith("${DB_PASSWORD:")
                .contains("YOUR_DEV_DB_PASSWORD_HERE");
        assertThat(props.getProperty("spring.jpa.hibernate.ddl-auto"))
                .contains("update");
    }

    @Test
    @DisplayName(".gitignore should exclude application.properties and .env from version control")
    void gitignoreShouldProtectLocalCredentials() throws Exception {
        String gitignoreContent = Files.readString(Path.of(".gitignore"));

        assertThat(gitignoreContent).contains("application.properties");
        assertThat(gitignoreContent).contains(".env");
    }
}

