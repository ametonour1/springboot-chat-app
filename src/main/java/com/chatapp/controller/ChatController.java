package com.chatapp.controller;

import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import com.chatapp.dto.ChatMessage;
import com.chatapp.dto.UserSearchDto;
import com.chatapp.kafka.ChatProducer;
import com.chatapp.service.UserService;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    // Test endpoint to verify that the HTTP server is running
    @GetMapping("/api/test/public/hello")
    public String hello() {
        return "Welcome to the Chat App!";
    }

    private final ChatProducer chatProducer;
    private UserService userService;

    public ChatController(UserService userService,ChatProducer chatProducer) {
        this.chatProducer = chatProducer;
        this.userService = userService;
    }

    @PostMapping("/api/test/public/send")
    public void sendMessage(@RequestBody ChatMessage message) {
        message.setTimestamp(System.currentTimeMillis());
        chatProducer.sendMessage(message);
    }

    @GetMapping("/search-user")
    public ResponseEntity<List<UserSearchDto>> searchUsersprivateEndpoint(Authentication authentication ,@RequestParam String username) {

          List<UserSearchDto> results = userService.findUsersByUsernamePrefix(username);
          return ResponseEntity.ok(results);
    }
}
