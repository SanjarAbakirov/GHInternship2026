package com.example.demo.dto;

import java.time.LocalDateTime;

public class ChatResponse {

    private String reply;
    private Long conversationId;
    private String conversationTitle;
    private boolean newConversation;
    private LocalDateTime timestamp;

    public ChatResponse() {
    }

    public ChatResponse(String reply) {
        this.reply = reply;
    }

    public ChatResponse(String reply, Long conversationId) {
        this.reply = reply;
        this.conversationId = conversationId;
    }

    public ChatResponse(
            String reply,
            Long conversationId,
            String conversationTitle,
            boolean newConversation,
            LocalDateTime timestamp) {
        this.reply = reply;
        this.conversationId = conversationId;
        this.conversationTitle = conversationTitle;
        this.newConversation = newConversation;
        this.timestamp = timestamp;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getConversationTitle() {
        return conversationTitle;
    }

    public void setConversationTitle(String conversationTitle) {
        this.conversationTitle = conversationTitle;
    }

    public boolean isNewConversation() {
        return newConversation;
    }

    public void setNewConversation(boolean newConversation) {
        this.newConversation = newConversation;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
