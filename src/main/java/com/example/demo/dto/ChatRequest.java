package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

public class ChatRequest {

    @NotBlank(message = "Message cannot be empty")
    private String message;

    /** Optional existing conversation id; omit/null to start a new conversation. */
    private Long conversationId;

    /** Optional AI model override; only applied when starting a new conversation. */
    private String modelName;

    public ChatRequest() {
    }

    public ChatRequest(String message) {
        this.message = message;
    }

    public ChatRequest(String message, Long conversationId) {
        this.message = message;
        this.conversationId = conversationId;
    }

    public ChatRequest(String message, Long conversationId, String modelName) {
        this.message = message;
        this.conversationId = conversationId;
        this.modelName = modelName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
}
