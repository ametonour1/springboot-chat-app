package com.chatapp.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import com.chatapp.dto.ChatMessageDTO;


@Service
public class ChatKafkaProducer {

    private final KafkaTemplate<String, ChatMessageDTO> kafkaTemplate;

    @Value("${kafka.topic.chat}")
    private String chatTopic;

    public ChatKafkaProducer(KafkaTemplate<String, ChatMessageDTO> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(ChatMessageDTO message) {
        kafkaTemplate.send(chatTopic, message.getMessageId(), message);
        //log.info("Kafka - Sent messageId={} from {} to {}", message.getMessageId(), message.getFrom(), message.getTo());
    }
}
