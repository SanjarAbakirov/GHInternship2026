package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

public class ChatRequest {

    @NotBlank(message = "Message cannot be empty")
    private String message;

    /** Optional existing session id; omit/null to start a new conversation. */
    private Long chatSessionId;

    public ChatRequest() {
    }

    public ChatRequest(String message) {
        this.message = message;
    }

    public ChatRequest(String message, Long chatSessionId) {
        this.message = message;
        this.chatSessionId = chatSessionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getChatSessionId() {
        return chatSessionId;
    }

    public void setChatSessionId(Long chatSessionId) {
        this.chatSessionId = chatSessionId;
    }
}
