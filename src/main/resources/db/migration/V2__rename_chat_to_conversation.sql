-- Renames the chat_sessions/chat_messages tables introduced in V1 to
-- conversations/messages to match the Conversation/Message JPA entities,
-- and adds the new model_name column used to record which AI model a
-- conversation was created with.

ALTER TABLE chat_sessions RENAME TO conversations;
ALTER TABLE chat_messages RENAME TO messages;

ALTER TABLE messages RENAME COLUMN session_id TO conversation_id;

ALTER TABLE conversations ADD COLUMN model_name VARCHAR(100);

ALTER TABLE conversations RENAME CONSTRAINT fk_chat_sessions_user TO fk_conversations_user;
ALTER TABLE messages RENAME CONSTRAINT fk_chat_messages_session TO fk_messages_conversation;

ALTER INDEX idx_chat_sessions_user_id RENAME TO idx_conversations_user_id;
ALTER INDEX idx_chat_messages_session_id RENAME TO idx_messages_conversation_id;
ALTER INDEX idx_chat_messages_session_created_at RENAME TO idx_messages_conversation_created_at;
