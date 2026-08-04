package com.example.demo.dto;

import java.time.LocalDateTime;

public class MessageResponse {

    private Long id;
    private String content;
    private String role;
    private LocalDateTime createdAt;

    public MessageResponse() {
    }

    public MessageResponse(Long id, String content, String role, LocalDateTime createdAt) {
        this.id = id;
        this.content = content;
        this.role = role;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
