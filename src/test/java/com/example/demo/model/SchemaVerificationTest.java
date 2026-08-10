package com.example.demo.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Step 7.1 (Testing & Verification): confirms the schema Hibernate actually creates matches what
 * the {@link User}/{@link Conversation}/{@link Message} entities expect -- tables exist, required
 * columns are NOT NULL, and foreign keys link the tables correctly. Runs against the ephemeral H2
 * test database configured in {@code application-test.properties}.
 */
@DataJpaTest
class SchemaVerificationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void allEntityTablesExist() {
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'", String.class);

        assertTrue(tables.contains("USERS"), "users table missing");
        assertTrue(tables.contains("CONVERSATIONS"), "conversations table missing");
        assertTrue(tables.contains("MESSAGES"), "messages table missing");
    }

    @Test
    void requiredColumnsAreNotNullable() {
        assertColumnNotNull("USERS", "USERNAME");
        assertColumnNotNull("USERS", "EMAIL");
        assertColumnNotNull("USERS", "PASSWORD");
        assertColumnNotNull("CONVERSATIONS", "TITLE");
        assertColumnNotNull("CONVERSATIONS", "USER_ID");
        assertColumnNotNull("CONVERSATIONS", "CREATED_AT");
        assertColumnNotNull("CONVERSATIONS", "UPDATED_AT");
        assertColumnNotNull("MESSAGES", "CONTENT");
        assertColumnNotNull("MESSAGES", "ROLE");
        assertColumnNotNull("MESSAGES", "CONVERSATION_ID");

        // model_name is intentionally optional: conversations created before this column existed
        // have no value for it (see V2 migration / Conversation.modelName javadoc).
        assertEquals("YES", nullability("CONVERSATIONS", "MODEL_NAME"));
    }

    @Test
    void foreignKeysLinkMessagesToConversationsAndConversationsToUsers() {
        assertForeignKeyExists("MESSAGES", "CONVERSATION_ID", "CONVERSATIONS");
        assertForeignKeyExists("CONVERSATIONS", "USER_ID", "USERS");
    }

    @Test
    void foreignKeysDoNotCascadeAtTheDatabaseLevel() {
        // Cascade delete (Conversation -> its Messages) is enforced by JPA
        // (CascadeType.ALL + orphanRemoval on Conversation.messages) issuing explicit DELETE
        // statements, not by an ON DELETE CASCADE clause in the schema itself. A raw SQL
        // DELETE FROM conversations without first removing its messages would violate this FK.
        // ConversationPersistenceTest#cascadeDelete_removesMessagesWhenConversationDeleted covers
        // the app-level behavior that compensates for this.
        List<String> deleteRules = jdbcTemplate.queryForList(
                "SELECT DELETE_RULE FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS", String.class);

        assertTrue(!deleteRules.isEmpty() && deleteRules.stream().allMatch("NO ACTION"::equals),
                "Expected no DB-level ON DELETE CASCADE; cascade is handled by Hibernate/JPA instead");
    }

    private void assertColumnNotNull(String table, String column) {
        assertEquals("NO", nullability(table, column), table + "." + column + " should be NOT NULL");
    }

    private String nullability(String table, String column) {
        return jdbcTemplate.queryForObject(
                "SELECT IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND COLUMN_NAME = ?",
                String.class, table, column);
    }

    private void assertForeignKeyExists(String fkTable, String fkColumn, String referencedTable) {
        List<String> matches = jdbcTemplate.queryForList(
                "SELECT kcu.CONSTRAINT_NAME "
                        + "FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE kcu "
                        + "JOIN INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc "
                        + "  ON tc.CONSTRAINT_NAME = kcu.CONSTRAINT_NAME AND tc.CONSTRAINT_TYPE = 'FOREIGN KEY' "
                        + "JOIN INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS rc "
                        + "  ON rc.CONSTRAINT_NAME = kcu.CONSTRAINT_NAME "
                        + "JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE ref "
                        + "  ON ref.CONSTRAINT_NAME = rc.UNIQUE_CONSTRAINT_NAME "
                        + "WHERE kcu.TABLE_NAME = ? AND kcu.COLUMN_NAME = ? AND ref.TABLE_NAME = ?",
                String.class, fkTable, fkColumn, referencedTable);

        assertTrue(!matches.isEmpty(),
                "Expected a foreign key " + fkTable + "." + fkColumn + " -> " + referencedTable + ".*");
    }
}
