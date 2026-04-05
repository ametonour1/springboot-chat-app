package com.chatapp.kafka;

import com.chatapp.dto.GroupReadReceiptEvent;
import com.chatapp.service.GroupChatService; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class GroupMessageStatusConsumer {

    @Autowired
    private GroupChatService groupChatService; 

    @KafkaListener(
        topics = "group-message-status-topic",
        groupId = "group-read-receipt-group",
        containerFactory = "groupReadReceiptKafkaListenerContainerFactory"
    )
    public void consumeGroupReadReceipt(GroupReadReceiptEvent event) {
        System.out.println("📥 Kafka Consumer grabbed read receipt from User " + event.getUserId() 
                + " up to msg " + event.getLastReadMessageId());
        
        groupChatService.handleGroupReadReceiptOrchestration(event);
    }
}