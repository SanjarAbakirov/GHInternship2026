package com.ghInternship.GHInternship2026.config;

import org.hibernate.dialect.PostgreSQLDialect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Driver;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты конфигурации драйвера PostgreSQL, профилей окружения (dev, prod)
 * и безопасности настроек DataSource.
 * Проверяют спецификацию JDBC URL (jdbc:postgresql://host:port/database),
 * разделение ddl-auto (update для dev, validate для prod) и защиту секретов.
 */
class PostgreSqlConfigurationTest {

    private static final Pattern POSTGRES_JDBC_URL_PATTERN =
            Pattern.compile("^jdbc:postgresql://([a-zA-Z0-9.-]+):(\\d{1,5})/([a-zA-Z0-9_]+)$");

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
    @DisplayName("PostgreSQL JDBC URL format should follow jdbc:postgresql://<host>:<port>/<database>")
    void jdbcUrlFormatShouldBeValid() {
        String defaultUrl = "jdbc:postgresql://localhost:5432/gh_internship_db";
        Matcher matcher = POSTGRES_JDBC_URL_PATTERN.matcher(defaultUrl);

        assertThat(matcher.matches()).isTrue();
        assertThat(matcher.group(1)).isEqualTo("localhost");
        assertThat(Integer.parseInt(matcher.group(2))).isEqualTo(5432);
        assertThat(matcher.group(3)).isEqualTo("gh_internship_db");
    }

    @Test
    @DisplayName("application.properties.example should configure PostgreSQL securely via env vars without real passwords")
    void examplePropertiesShouldUsePostgreSqlAndEnvPlaceholders() throws Exception {
        Path examplePath = Path.of("src/main/resources/application.properties.example");
        assertThat(Files.exists(examplePath)).isTrue();

        Properties props = loadProperties(examplePath);

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
        assertThat(props.getProperty("spring.h2.console.enabled"))
                .isEqualTo("false");
    }

    @Test
    @DisplayName("application-dev.properties should configure PostgreSQL with ddl-auto=update and SQL logging")
    void devProfileShouldConfigurePostgreSqlWithUpdateDdl() throws Exception {
        Path devPath = Path.of("src/main/resources/application-dev.properties");
        assertThat(Files.exists(devPath)).isTrue();

        Properties props = loadProperties(devPath);

        assertThat(props.getProperty("spring.datasource.driver-class-name"))
                .isEqualTo("org.postgresql.Driver");
        assertThat(props.getProperty("spring.datasource.url"))
                .contains("jdbc:postgresql://localhost:5432/gh_internship_db");
        assertThat(props.getProperty("spring.datasource.username"))
                .contains("gh_app_user");
        assertThat(props.getProperty("spring.jpa.hibernate.ddl-auto"))
                .contains("update");
        assertThat(props.getProperty("spring.jpa.show-sql"))
                .contains("true");
        assertThat(props.getProperty("spring.h2.console.enabled"))
                .isEqualTo("false");
    }

    @Test
    @DisplayName("application-prod.properties should configure PostgreSQL with ddl-auto=validate for safer schema management")
    void prodProfileShouldConfigurePostgreSqlWithValidateDdl() throws Exception {
        Path prodPath = Path.of("src/main/resources/application-prod.properties");
        assertThat(Files.exists(prodPath)).isTrue();

        Properties props = loadProperties(prodPath);

        assertThat(props.getProperty("spring.datasource.driver-class-name"))
                .isEqualTo("org.postgresql.Driver");
        assertThat(props.getProperty("spring.datasource.url"))
                .isEqualTo("${DB_URL}");
        assertThat(props.getProperty("spring.datasource.username"))
                .isEqualTo("${DB_USERNAME}");
        assertThat(props.getProperty("spring.datasource.password"))
                .isEqualTo("${DB_PASSWORD}");
        // Безопасное управление схемой в production — validate предотвращает случайные изменения таблиц
        assertThat(props.getProperty("spring.jpa.hibernate.ddl-auto"))
                .isEqualTo("validate");
        assertThat(props.getProperty("spring.jpa.show-sql"))
                .isEqualTo("false");
        assertThat(props.getProperty("spring.h2.console.enabled"))
                .isEqualTo("false");
    }

    @Test
    @DisplayName(".gitignore should exclude application.properties and .env from version control")
    void gitignoreShouldProtectLocalCredentials() throws Exception {
        String gitignoreContent = Files.readString(Path.of(".gitignore"));

        assertThat(gitignoreContent).contains("application.properties");
        assertThat(gitignoreContent).contains(".env");
    }

    private Properties loadProperties(Path path) throws Exception {
        Properties props = new Properties();
        try (var reader = Files.newBufferedReader(path)) {
            props.load(reader);
        }
        return props;
    }
}
