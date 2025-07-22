package com.chatapp.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.Message;


import com.chatapp.dto.ChatMessage;
import com.chatapp.dto.SocketHandshakeMessage;
import com.chatapp.dto.SocketHeartbeatMessage;
import com.chatapp.service.RedisService;
import com.chatapp.service.SessionTracker;
import com.chatapp.kafka.ChatProducer;


@Controller
public class SocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisService redisService;
    private final ChatProducer chatProducer;
    private final SessionTracker sessionTracker;


    @Autowired
   public SocketController(SimpMessagingTemplate messagingTemplate,
                        RedisService redisService,
                        ChatProducer chatProducer,
                        SessionTracker sessionTracker) {
    this.messagingTemplate = messagingTemplate;
    this.redisService = redisService;
    this.chatProducer = chatProducer;  
    this.sessionTracker = sessionTracker;
}
  
    @MessageMapping("/register")
    public void registerUser(@Payload SocketHandshakeMessage message, 
                         SimpMessageHeaderAccessor headerAccessor) {
    String sessionId = headerAccessor.getSessionId();
    String userId = message.getUserId();

    // Now you can store the mapping userId -> sessionId in Redis
    redisService.storeUserSocketSession(userId, sessionId);

     Long connectionsCount = redisService.getUserConnectionCount("user:sockets:" + userId);
    if (connectionsCount == 1) {
        // First connection for this user, broadcast online status
        //notifyRecentChattersUserIsOnline(userId);
        System.out.println("USER first connection  " + userId );
        //redisService.markUserOnline(userId);

    }

    System.out.println("Registered user " + userId + " with session " + sessionId);
}
    @MessageMapping("/heartbeat")
    public void handleHeartbeat(@Payload SocketHeartbeatMessage heartbeat) {
        //redisService.markUserOnline(heartbeat.getUserId()); // Reset TTL
        Long userId = Long.parseLong(heartbeat.getUserId());
        sessionTracker.updateHeartbeat(userId);
        System.out.println("Heartbeat received for " + heartbeat.getUserId());
    }

    @MessageMapping("/chat.send")
    public void handleChatMessage(@Payload ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        System.out.println("Message received from session: " + sessionId);

        chatProducer.sendMessage(message);
    }

 
}
