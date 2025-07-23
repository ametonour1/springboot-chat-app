package com.chatapp.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.chatapp.dto.UserStatusChangedEvent;
import com.chatapp.service.SessionTracker;
@Service
public class UserStatusConsumer {


    private final SessionTracker sessionTracker;

    public UserStatusConsumer(SessionTracker sessionTracker) {
        this.sessionTracker = sessionTracker;
    }

    @KafkaListener(topics = "user-status-changes", groupId = "status-group", containerFactory = "userStatusKafkaListenerContainerFactory")
    public void consumeStatusChange(UserStatusChangedEvent event) {
        String userId = event.getUserId();
        boolean isOnline = event.isOnline();
        sessionTracker.emitUserStatus(userId,isOnline);
    }
}
