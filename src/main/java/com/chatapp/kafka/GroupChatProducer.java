package com.chatapp.kafka;

import com.chatapp.dto.GroupChatMessageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class GroupChatProducer {

    private final KafkaTemplate<String, GroupChatMessageRequest> kafkaTemplate;
    private static final String TOPIC = "group-chat-messages";

    public GroupChatProducer(KafkaTemplate<String, GroupChatMessageRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(GroupChatMessageRequest message) {
        kafkaTemplate.send(TOPIC, message);
        System.out.println("Sent group chat message to Kafka: " + message.getContent());
    }
}
