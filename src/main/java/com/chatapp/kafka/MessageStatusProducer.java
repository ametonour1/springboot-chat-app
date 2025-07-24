package com.chatapp.kafka;

import com.chatapp.dto.MessageStatusEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageStatusProducer {

    private static final String TOPIC = "message.status";

    private final KafkaTemplate<String, MessageStatusEvent> kafkaTemplate;

    @Autowired
    public MessageStatusProducer(KafkaTemplate<String, MessageStatusEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendStatusUpdate(MessageStatusEvent event) {
        kafkaTemplate.send(TOPIC, event);
    }
}
