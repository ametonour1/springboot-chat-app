package com.chatapp.kafka;


import com.chatapp.dto.ChatMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatProducer {

    @Value("${kafka.topic.chat}")
    private String chatTopic;

    private final KafkaTemplate<String, ChatMessage> kafkaTemplate;

    public ChatProducer(KafkaTemplate<String, ChatMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(ChatMessage message) {
        kafkaTemplate.send(chatTopic, message.getRecipientUsername(), message);
    }
}
