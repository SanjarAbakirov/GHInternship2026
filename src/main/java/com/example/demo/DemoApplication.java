package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }



    @GetMapping("/hello")
    public String sayHello() {
        return "Hello World! This is new project!";
    }
}

// business logic// diff interface and class
// diff interface and class
// copy all with command - ask AI about (claud.ai) - ask curiously
//interface - working with db