package com.chatapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.chatapp.dto.ChatMessage;
import com.chatapp.dto.SocketHandshakeMessage;
import com.chatapp.service.RedisService;

@Controller
public class SocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisService redisService;

    @Autowired
    public SocketController(SimpMessagingTemplate messagingTemplate, RedisService redisService) {
        this.messagingTemplate = messagingTemplate;
        this.redisService = redisService;
    }
    // Handle handshake / connection event, e.g. when client connects
    @MessageMapping("/register")
    public void registerUser(@Payload SocketHandshakeMessage message, 
                         SimpMessageHeaderAccessor headerAccessor) {
    String sessionId = headerAccessor.getSessionId();
    String userId = message.getUserId();

    // Now you can store the mapping userId -> sessionId in Redis
    redisService.storeUserSocketSession(userId, sessionId);

    System.out.println("Registered user " + userId + " with session " + sessionId);
}


}
