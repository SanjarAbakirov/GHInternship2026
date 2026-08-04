package com.example.demo.dto;

import java.time.LocalDateTime;

public class ConversationResponse {

    private Long id;
    private String title;
    private String modelName;
    private LocalDateTime createdAt;

    public ConversationResponse() {
    }

    public ConversationResponse(Long id, String title, String modelName, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.modelName = modelName;
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
}
