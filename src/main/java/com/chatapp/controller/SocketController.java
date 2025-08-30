package com.chatapp.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.Message;

import com.chatapp.dto.CachedMessagesRequest;
import com.chatapp.dto.ChatMessage;
import com.chatapp.dto.ChatMessageReadDto;
import com.chatapp.dto.GroupChatMessageRequest;
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
import com.chatapp.kafka.GroupChatProducer;


@Controller
public class SocketController {

    private final ChatService chatService;

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisService redisService;
    private final ChatProducer chatProducer;
    private final MessageStatusProducer messageStatusProducer;
    private final SessionTracker sessionTracker;
    private final GroupChatProducer groupChatProducer;



    @Autowired
   public SocketController(SimpMessagingTemplate messagingTemplate,
                        RedisService redisService,
                        ChatProducer chatProducer,
                        SessionTracker sessionTracker, ChatService chatService,
                        MessageStatusProducer messageStatusProducer,
                        GroupChatProducer groupChatProducer) {
    this.messagingTemplate = messagingTemplate;
    this.redisService = redisService;
    this.chatProducer = chatProducer;  
    this.sessionTracker = sessionTracker;  
    this.chatService = chatService;
    this.messageStatusProducer = messageStatusProducer;
    this.groupChatProducer = groupChatProducer;
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
    System.out.println(userIdStr+ "userIdStieng");

    //chatService.markMessageAsRead(dto.getMessageId(), userId);
    MessageStatusEvent event = new MessageStatusEvent();
    event.setMessageId(dto.getMessageId());
    //event.setSenderId(userId);
    event.setRecipientId(userId);
    event.setStatus(MessageStatus.READ);  // or the string "READ" if you use strings

    // Send event to Kafka
    messageStatusProducer.sendStatusUpdate(event);
}


   @MessageMapping("/get-cached-messages")
    public void getCachedMessages(@Payload CachedMessagesRequest request) {
       Long senderId = request.getSenderId();
    Long recipientId = request.getRecipientId();
    int offset = request.getOffset() != null ? request.getOffset() : 0;
    int limit = request.getLimit() != null ? request.getLimit() : 10;

    final int MAX_CACHED_MESSAGES = 10;

    List<ChatMessageEntity> resultMessages = new ArrayList<>();

    if (offset >= MAX_CACHED_MESSAGES) {
        System.out.println("offser is > max chaage retuning only from db, offset:" + offset );

        // Fetch only from DB with adjusted offset
        int dbSkip = offset;
        resultMessages = new ArrayList<>(chatService.getMessagesBetweenUsers(senderId, recipientId, dbSkip, limit));
        Collections.reverse(resultMessages);

    } else {
        System.out.println("fetching chached messaeges,  offset:" + offset );

        // Fetch from cache first
        List<ChatMessageEntity> cachedMessages = redisService.getCachedMessages(senderId, recipientId);

        int skipCached = offset;

        List<ChatMessageEntity> cachedSlice = cachedMessages.stream()
            .skip(skipCached)
            .limit(limit)
            .collect(Collectors.toList());

        System.out.println("the cagcgedSlice" + cachedSlice );


        int cachedReturned = cachedSlice.size();
                System.out.println("cache returned:" + cachedReturned);
        

        // If cached messages are less than limit, fetch the rest from DB
        if (cachedReturned < limit) {
           int remaining = limit - cachedReturned;
            int dbSkip = offset + cachedReturned; // Skip already returned cached + offset
        System.out.println("the chaged messaegs are not engouh retuning remaing from db, dbSkip:" + dbSkip+"remaining:" + remaining );

          List<ChatMessageEntity> dbMessages = new ArrayList<>(chatService.getMessagesBetweenUsers(senderId, recipientId, dbSkip, remaining));
            Collections.reverse(dbMessages); 
            resultMessages.addAll(dbMessages);
            

        }
        resultMessages.addAll(cachedSlice);
    }

        // Send messages back to the requesting user
        System.out.println("the cached messaegs resultMessages " + resultMessages);

        String destination = "/topic/cached-messages/" + senderId;
        messagingTemplate.convertAndSend(destination, resultMessages);
    }
 
    //group chats


    @MessageMapping("/group-chat/chat.send")
    public void handleGroupChatMessage(@Payload GroupChatMessageRequest message, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        System.out.println("Message received from session: " + sessionId);
        groupChatProducer.sendMessage(message);

    }
}
