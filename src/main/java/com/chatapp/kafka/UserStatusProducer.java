package com.chatapp.kafka;
import com.chatapp.dto.ChatMessage;
import com.chatapp.dto.UserStatusChangedEvent;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserStatusProducer {
    
    private final KafkaTemplate<String, UserStatusChangedEvent> kafkaTemplate;

    public UserStatusProducer(KafkaTemplate<String, UserStatusChangedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

     public void broadcastUserStatusChange(String userId, boolean isOnline) {
        UserStatusChangedEvent event = new UserStatusChangedEvent(userId, isOnline);
        kafkaTemplate.send("user-status-changes", event);
    }
}
