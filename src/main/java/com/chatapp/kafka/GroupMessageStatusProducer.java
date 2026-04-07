package com.chatapp.kafka;

import com.chatapp.dto.GroupReadReceiptEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class GroupMessageStatusProducer {

    @Autowired
    // 🚀 TELL SPRING EXACTLY WHICH BEAN TO USE BY ITS METHOD NAME IN KAFKACONFIG!
    @Qualifier("groupReadReceiptKafkaTemplate")
    private KafkaTemplate<String, GroupReadReceiptEvent> kafkaTemplate;

    private static final String TOPIC = "group-message-status-topic";

    public void sendGroupReadReceipt(GroupReadReceiptEvent event) {
        String partitionKey = String.valueOf(event.getGroupChatId());
        
        kafkaTemplate.send(TOPIC, partitionKey, event);
        System.out.println("Dispatched group read receipt to Kafka for group: " + event.getGroupChatId());
    }
}