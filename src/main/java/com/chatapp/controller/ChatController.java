package com.chatapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    // Test endpoint to verify that the HTTP server is running
    @GetMapping("/hello")
    public String hello() {
        return "Welcome to the Chat App!";
    }
}
