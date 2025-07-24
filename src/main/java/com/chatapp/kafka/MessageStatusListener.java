package com.chatapp.kafka;

import com.chatapp.dto.MessageStatusEvent;
import com.chatapp.model.MessageStatus;
import com.chatapp.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MessageStatusListener {

    private final ChatService chatService;

    @Autowired
    public MessageStatusListener(ChatService chatService) {
        this.chatService = chatService;
    }

    @KafkaListener(
        topics = "message.status",
        groupId = "status-group",
        containerFactory = "messageStatusKafkaListenerContainerFactory"
    )
    public void listen(MessageStatusEvent event) {
           if (event.getStatus() == MessageStatus.READ) {
                chatService.handleMessageStatusUpdate(event);
    } else if (event.getStatus() == MessageStatus.DELIVERED) {
        chatService.handleMessageDeliveryStatus(event);
    } else {
      throw new RuntimeException("event type not handled" + event.getStatus());

    }
    }

    
}
