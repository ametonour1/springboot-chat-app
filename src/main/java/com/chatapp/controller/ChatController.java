package com.chatapp.controller;

import com.chatapp.dto.ChatMessageDTO;
import com.chatapp.dto.ChatSocketMessage;
import com.chatapp.dto.ChatSocketTestMessage;
import com.chatapp.service.ChatKafkaProducer;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;

import java.security.Principal;
import java.time.Instant;
import java.util.UUID;




@Controller // Use @Controller for WebSocket endpoints
public class ChatController {



    @javax.annotation.PostConstruct
    public void init() {
        System.out.println(">>> ChatController LOADED <<<");
    }

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

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public ChatSocketMessage sendTestMessage(@Payload String messagePayload) {
        // Log the raw payload content
        System.out.println("Raw Payload: " + messagePayload);
    
        // Assuming the payload is a JSON string that we need to convert to ChatSocketTestMessage
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            
            // Map the payload to the ChatSocketTestMessage object
            ChatSocketTestMessage message = objectMapper.readValue(messagePayload, ChatSocketTestMessage.class);
            System.out.println("Message content: " + message.getText());
    
            // Create a new ChatSocketMessage
            ChatSocketMessage chatSocketMessage = new ChatSocketMessage();
            
            // Set the text in the ChatSocketMessage using setText
            chatSocketMessage.setText(message.getText()); // Use the setText method
            
            // Return the constructed ChatSocketMessage
            return chatSocketMessage;
        } catch (Exception e) {
            System.err.println("Error parsing message payload: " + e.getMessage());
            // Return a default error message as a fallback
            ChatSocketMessage errorMessage = new ChatSocketMessage();
            errorMessage.setText("Error processing the message.");
            return errorMessage;
        }
    }
    @SubscribeMapping("/topic/messages")
    public ChatSocketMessage sendMessageToClient() {
        // Create an instance of ChatSocketMessage
        ChatSocketMessage message = new ChatSocketMessage();
        
        // Set the text using the setter method
        message.setText("Welcome to the chat!");
    
        // Optionally, you can set the recipient (if required)
        //message.setTo("someUserId"); // Adjust as needed
        
        return message;
    }
    @MessageMapping("/test")
    public String testMessage(@Payload String message, Principal principal) {
        System.out.println("Test message received: " + message);  // This should print if method is hit
        return message;  // This will send the message back to the sender directly
    }

}
