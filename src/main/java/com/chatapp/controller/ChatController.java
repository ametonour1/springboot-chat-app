package com.chatapp.controller;

import com.chatapp.dto.ChatMessageDTO;
import com.chatapp.dto.ChatSocketMessage;
import com.chatapp.service.ChatKafkaProducer;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;

@Controller // Use @Controller for WebSocket endpoints
public class ChatController {

    private final ChatKafkaProducer kafkaProducer;

    public ChatController(ChatKafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @MessageMapping("/chat/send") // This maps to the client-sent destination: /app/chat/send
    public void sendMessage(@Payload ChatSocketMessage socketMessage, Principal principal) {
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setFrom(principal.getName()); // the user sending the message
        dto.setTo(socketMessage.getTo()); // the user receiving the message
        dto.setText(socketMessage.getText());
        dto.setMessageId(UUID.randomUUID().toString());
        dto.setTimestamp(Instant.now().toEpochMilli());

        kafkaProducer.sendMessage(dto); // send message to Kafka topic
    }
}
