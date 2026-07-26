package com.example.demo.dto;

public class ChatResponse {

    private String reply;
    private Long chatSessionId;

    public ChatResponse() {
    }

    public ChatResponse(String reply) {
        this.reply = reply;
    }

    public ChatResponse(String reply, Long chatSessionId) {
        this.reply = reply;
        this.chatSessionId = chatSessionId;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public Long getChatSessionId() {
        return chatSessionId;
    }

    public void setChatSessionId(Long chatSessionId) {
        this.chatSessionId = chatSessionId;
    }
}
