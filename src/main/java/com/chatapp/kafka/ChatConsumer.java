package com.chatapp.kafka;

import com.chatapp.dto.ChatMessage;
import com.chatapp.dto.RecentChatterDto;
import com.chatapp.dto.UserStatusChangedEvent;
import com.chatapp.service.ChatService;
import com.chatapp.service.RedisService;
import com.chatapp.service.SessionTracker;


import java.util.List;
import java.util.Set;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ChatConsumer {

    private final SessionTracker sessionTracker;

    private final ChatService chatService;
    private final RedisService redisService;

     public ChatConsumer(ChatService chatService, RedisService redisService, SessionTracker sessionTracker) {
        this.chatService = chatService;
        this.redisService = redisService;
        this.sessionTracker = sessionTracker;
    }


    @KafkaListener(topics = "${kafka.topic.chat}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(ChatMessage message) {
        System.out.println("Received message from: " + message.getSenderId() + 
                           " to: " + message.getRecipientId() +
                           " -> " + message.getContent());
           System.out.println("hello: ");
           chatService.sendMessageToUser(message.getRecipientId().toString(), message);
           chatService.updateRecentChats(message.getSenderId().toString(), message.getRecipientId().toString());
           chatService.pushRecentChatUpdates(
                message.getSenderId().toString(),
                message.getRecipientId().toString()
            );
       
    }

      @KafkaListener(topics = "user-status-changes", groupId = "status-group")
     public void consumeStatusChange(UserStatusChangedEvent event) {
        sessionTracker.emitUserStatus(event.getUserId(), event.isOnline());
    }
}
