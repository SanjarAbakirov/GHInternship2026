package com.example.demo.dto;

import java.time.LocalDateTime;

public class ChatSessionResponse {

    private Long id;
    private String title;
    private LocalDateTime createdAt;

    public ChatSessionResponse() {
    }

    public ChatSessionResponse(Long id, String title, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.createdAt = createdAt;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
