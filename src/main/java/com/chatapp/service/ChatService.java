package com.chatapp.service;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.chatapp.dto.ChatMessage;

@Service
public class ChatService {

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisService redisService;

    public ChatService(SimpMessagingTemplate messagingTemplate, RedisService redisService) {
        this.messagingTemplate = messagingTemplate;
        this.redisService = redisService;
    }

    // Send message to user if online
    public void sendMessageToUser(String userId, ChatMessage message) {
        if (redisService.isUserOnline(userId)) {
            messagingTemplate.convertAndSend("/topic/messages/" + userId, message);
        } else {
            // handle offline user case here (store message or log)
            System.out.println("User " + userId + " is offline. Consider storing message.");
        }
    }
}
