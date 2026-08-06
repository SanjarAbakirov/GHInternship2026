package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Conversation metadata plus its full ordered message list, returned when a
 * single conversation is opened (e.g. loading chat history into the UI).
 */
public class ConversationDetailResponse {

    private Long id;
    private String title;
    private String modelName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int messageCount;
    private List<MessageResponse> messages;

    public ConversationDetailResponse() {
    }

    public ConversationDetailResponse(
            Long id,
            String title,
            String modelName,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            List<MessageResponse> messages) {
        this.id = id;
        this.title = title;
        this.modelName = modelName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.messages = messages;
        this.messageCount = messages == null ? 0 : messages.size();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public List<MessageResponse> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageResponse> messages) {
        this.messages = messages;
    }
}
