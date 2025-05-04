package com.chatapp.controller;

import com.chatapp.dto.ChatMessageDTO;
import com.chatapp.dto.ChatSocketMessage;
import com.chatapp.service.ChatKafkaProducer;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.SendTo;

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
        System.out.println("Message recived");

        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setFrom(principal.getName()); // the user sending the message
        dto.setTo(socketMessage.getTo()); // the user receiving the message
        dto.setText(socketMessage.getText());
        dto.setMessageId(UUID.randomUUID().toString());
        dto.setTimestamp(Instant.now().toEpochMilli());

        kafkaProducer.sendMessage(dto); // send message to Kafka topic
    }

    @MessageMapping("/chat/sendTest")
    @SendTo("/topic/messages") 
    public ChatSocketMessage sendTestMessage(@Payload ChatSocketMessage socketMessage, Principal principal) {
        System.out.println("Message received from: " + principal.getName());
        System.out.println("Message content: " + socketMessage.getText());
        System.out.println("Message recived");


        // Add test metadata
        
        return socketMessage;
    }
    @MessageMapping("/test")
    public String testMessage(@Payload String message, Principal principal) {
        System.out.println("Test message received: " + message);  // This should print if method is hit
        return message;  // This will send the message back to the sender directly
    }

    @MessageMapping("/chat/raw")
    public void receiveRawMessage(@Payload String message) {
        System.out.println("RAW MESSAGE: " + message);
    }
}
