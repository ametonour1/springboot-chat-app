package com.chatapp.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.Message;


import com.chatapp.dto.ChatMessage;
import com.chatapp.dto.ChatMessageReadDto;
import com.chatapp.dto.MessageStatusEvent;
import com.chatapp.dto.SocketHandshakeMessage;
import com.chatapp.dto.SocketHeartbeatMessage;
import com.chatapp.service.ChatService;
import com.chatapp.service.RedisService;
import com.chatapp.service.SessionTracker;
import com.chatapp.kafka.ChatProducer;
import com.chatapp.kafka.MessageStatusProducer;
import com.chatapp.model.ChatMessageEntity;
import com.chatapp.model.MessageStatus;


@Controller
public class SocketController {

    private final ChatService chatService;

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisService redisService;
    private final ChatProducer chatProducer;
    private final MessageStatusProducer messageStatusProducer;
    private final SessionTracker sessionTracker;


    @Autowired
   public SocketController(SimpMessagingTemplate messagingTemplate,
                        RedisService redisService,
                        ChatProducer chatProducer,
                        SessionTracker sessionTracker, ChatService chatService,
                        MessageStatusProducer messageStatusProducer) {
    this.messagingTemplate = messagingTemplate;
    this.redisService = redisService;
    this.chatProducer = chatProducer;  
    this.sessionTracker = sessionTracker;  
    this.chatService = chatService;
    this.messageStatusProducer = messageStatusProducer;
}
  
    @MessageMapping("/register")
    public void registerUser(@Payload SocketHandshakeMessage message, 
                         SimpMessageHeaderAccessor headerAccessor) {
    String sessionId = headerAccessor.getSessionId();
    String userId = message.getUserId();

    // Now you can store the mapping userId -> sessionId in Redis
    redisService.storeUserSocketSession(userId, sessionId);

     Long connectionsCount = redisService.getUserConnectionCount("user:sockets:" + userId);
    if (connectionsCount == 1) {
        // First connection for this user, broadcast online status
        //notifyRecentChattersUserIsOnline(userId);
        System.out.println("USER first connection  " + userId );
        //redisService.markUserOnline(userId);
       

    }
        Long longUserId = Long.parseLong(userId);

        List<ChatMessageEntity> messages = chatService.markSentMessagesAsDeliveredAndGetLatestPerSender(longUserId);

        System.out.println("Delivered messages for user " + userId + ":");
        for (ChatMessageEntity msg : messages) {        
              MessageStatusEvent event = new MessageStatusEvent();
                event.setSenderId(msg.getSenderId());
                event.setRecipientId(msg.getRecipientId());
                event.setStatus(MessageStatus.DELIVERED);
                event.setMessageId(msg.getId());
                messageStatusProducer.sendStatusUpdate(event);
        }

    System.out.println("Registered user " + userId + " with session " + sessionId);
}
    @MessageMapping("/heartbeat")
    public void handleHeartbeat(@Payload SocketHeartbeatMessage heartbeat) {
        //redisService.markUserOnline(heartbeat.getUserId()); // Reset TTL
        Long userId = Long.parseLong(heartbeat.getUserId());
        sessionTracker.updateHeartbeat(userId);
        System.out.println("Heartbeat received for " + heartbeat.getUserId());
    }

    @MessageMapping("/chat.send")
    public void handleChatMessage(@Payload ChatMessage message, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        System.out.println("Message received from session: " + sessionId);

        chatProducer.sendMessage(message);
    }

   @MessageMapping("/message/read")
public void handleMarkAsRead(ChatMessageReadDto dto, @Header("simpSessionId") String sessionId) {
    String userIdStr = redisService.getUserIdFromSession(sessionId);
    if (userIdStr == null) {
        throw new RuntimeException("User session not found or expired.");
    }

    Long userId = Long.parseLong(userIdStr);

    //chatService.markMessageAsRead(dto.getMessageId(), userId);
    MessageStatusEvent event = new MessageStatusEvent();
    event.setMessageId(dto.getMessageId());
    //event.setSenderId(userId);
    event.setRecipientId(userId);
    event.setStatus(MessageStatus.READ);  // or the string "READ" if you use strings

    // Send event to Kafka
    messageStatusProducer.sendStatusUpdate(event);
}

 
}
