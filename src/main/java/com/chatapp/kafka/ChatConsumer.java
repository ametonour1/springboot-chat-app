package com.chatapp.kafka;

import com.chatapp.dto.ChatMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ChatConsumer {

    @KafkaListener(topics = "${kafka.topic.chat}", groupId = "${spring.kafka.consumer.group-id}")
    public void listen(ChatMessage message) {
        System.out.println("Received message from: " + message.getSenderUsername() + 
                           " to: " + message.getRecipientUsername() +
                           " -> " + message.getContent());
        
        // TODO: Emit to WebSocket if online, or store for later
    }
}
