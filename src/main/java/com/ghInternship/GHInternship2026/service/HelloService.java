package com.ghInternship.GHInternship2026.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HelloService {

    private static final Logger log = LoggerFactory.getLogger(HelloService.class);

    public String getGreeting() {
        log.info("HelloService: generating greeting message");
        return "Hello World!";
    }
}
