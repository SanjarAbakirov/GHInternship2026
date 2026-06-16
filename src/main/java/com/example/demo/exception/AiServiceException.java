package com.example.demo.exception;

public class AiServiceException extends RuntimeException {
    public AiServiceException(String message) {
        super(message);
    }
}