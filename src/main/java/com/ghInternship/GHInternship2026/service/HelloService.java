package com.ghInternship.GHInternship2026.service;

import org.springframework.stereotype.Service;

/**
 * HelloService — business logic layer for the HelloController.
 * Separates business logic from the web layer.
 */
@Service
public class HelloService {

    public String getGreeting() {
        return "Hello World!";
    }
}
