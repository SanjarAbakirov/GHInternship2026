package com.ghInternship.GHInternship2026.controller;

import com.ghInternship.GHInternship2026.service.HelloService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HelloController — handles incoming HTTP requests for the /hello endpoint.
 * Delegates business logic to HelloService.
 */
@RestController
public class HelloController {

    private final HelloService helloService;

    public HelloController(HelloService helloService) {
        this.helloService = helloService;
    }

    @GetMapping("/hello")
    public String hello() {
        return helloService.getGreeting();
    }
}
