package com.chatapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import com.chatapp.dto.ChatMessage;
import com.chatapp.kafka.ChatProducer;

@RestController
public class ChatController {

    // Test endpoint to verify that the HTTP server is running
    @GetMapping("/api/test/public/hello")
    public String hello() {
        return "Welcome to the Chat App!";
    }

     private final ChatProducer chatProducer;

    public ChatController(ChatProducer chatProducer) {
        this.chatProducer = chatProducer;
    }

    @PostMapping("/api/test/public/send")
    public void sendMessage(@RequestBody ChatMessage message) {
        message.setTimestamp(System.currentTimeMillis());
        chatProducer.sendMessage(message);
    }
}
