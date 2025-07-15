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
import com.chatapp.kafka.ChatProducer;


@Controller
public class SocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisService redisService;
    private final ChatProducer chatProducer;


    @Autowired
   public SocketController(SimpMessagingTemplate messagingTemplate,
                        RedisService redisService,
                        ChatProducer chatProducer) {
    this.messagingTemplate = messagingTemplate;
    this.redisService = redisService;
    this.chatProducer = chatProducer;  
}
  
    @MessageMapping("/register")
    public void registerUser(@Payload SocketHandshakeMessage message, 
                         SimpMessageHeaderAccessor headerAccessor) {
    String sessionId = headerAccessor.getSessionId();
    String userId = message.getUserId();

    // Now you can store the mapping userId -> sessionId in Redis
    redisService.storeUserSocketSession(userId, sessionId);

    System.out.println("Registered user " + userId + " with session " + sessionId);
}

    @MessageMapping("/chat.send")
    public void handleChatMessage(@Payload ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        System.out.println("Message received from session: " + sessionId);

        chatProducer.sendMessage(message);
    }

}
