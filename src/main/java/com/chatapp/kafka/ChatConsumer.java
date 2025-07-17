package com.chatapp.kafka;

import com.chatapp.dto.ChatMessage;
import com.chatapp.dto.RecentChatterDto;
import com.chatapp.service.ChatService;

import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ChatConsumer {

    private final ChatService chatService;

     public ChatConsumer(ChatService chatService) {
        this.chatService = chatService;
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
}
