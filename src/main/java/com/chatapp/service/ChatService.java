package com.chatapp.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.chatapp.dto.ChatMessage;
import com.chatapp.dto.MessageStatusEvent;
import com.chatapp.dto.RecentChatterDto;
import com.chatapp.model.ChatMessageEntity;
import com.chatapp.model.MessageStatus;
import com.chatapp.model.User;
import com.chatapp.repository.ChatMessageRepository;

@Service
public class ChatService {

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisService redisService;
    private final ChatMessageRepository chatMessageRepository;
    private final UserService userService;

    public ChatService(SimpMessagingTemplate messagingTemplate, RedisService redisService, ChatMessageRepository chatMessageRepository, UserService userService) {
        this.messagingTemplate = messagingTemplate;
        this.redisService = redisService;
        this.chatMessageRepository = chatMessageRepository;
        this.userService = userService;
    }

    // Send message to user if online
    public void sendMessageToUser(String userId, ChatMessage message) {

         ChatMessageEntity entity = mapDtoToEntity(message);


        if (redisService.isUserOnline(userId)) {
            entity.setStatus(MessageStatus.DELIVERED); 
            message.setStatus(MessageStatus.DELIVERED); 
            saveMessage(entity);

                ChatMessage msgToRecipient = new ChatMessage(
                entity.getId(),
                message.getSenderId(),
                message.getRecipientId(),
                message.getContent(),
                message.getTimestamp(),
                message.getStatus(),
                false // not sender
            );
            messagingTemplate.convertAndSend("/topic/messages/" + message.getRecipientId().toString(), msgToRecipient);
        } else {
            saveMessage(entity);
            message.setStatus(MessageStatus.SENT);
            // handle offline user case here (store message or log)
            System.out.println("User " + userId + " is offline. saved to DB.");
        }

            // Send to sender with me=true
            ChatMessage msgToSender = new ChatMessage(
                entity.getId(),
                message.getSenderId(),
                message.getRecipientId(),
                message.getContent(),
                message.getTimestamp(),
                message.getStatus(),
                true // this is sender's own message
            );
            messagingTemplate.convertAndSend("/topic/messages/" + message.getSenderId().toString(), msgToSender);
    }

    private ChatMessageEntity mapDtoToEntity(ChatMessage dto) {
    ChatMessageEntity entity = new ChatMessageEntity();
    entity.setSenderId(dto.getSenderId());
    entity.setRecipientId(dto.getRecipientId());
    entity.setContent(dto.getContent());
    entity.setStatus(MessageStatus.SENT); // or default status
    return entity;
}

    public ChatMessageEntity saveMessage(ChatMessageEntity message) {
        return chatMessageRepository.save(message);
    }

    public void updateRecentChats(String userId, String chatterId) {
        
      redisService.addRecentChatter(userId, chatterId);
    }

    public void returneRecentChats(String userId) {
        
      redisService.getRecentChatters(userId);
    }

  public List<RecentChatterDto> getRecentChattersWithDetails(String userId) {
        List<String> chatterIds = redisService.getRecentChatters(userId);
        List<User> users = userService.getUsersByIds(chatterIds);

        // Map users by ID for quick lookup
        Map<String, User> userMap = users.stream()
            .collect(Collectors.toMap(user -> user.getId().toString(), user -> user));

        // Rebuild the list in Redis order
        return chatterIds.stream()
            .map(userMap::get) // get user by ID
            .filter(Objects::nonNull) // skip nulls in case of missing users
            .map(user -> {
            boolean isOnline = redisService.isUserOnline(user.getId().toString());
            boolean hasUnreadMessages = hasUnreadMessagesFrom(
            user.getId().toString(), // sender
            userId                   // recipient (you)
        );;
            return new RecentChatterDto(user.getId().toString(), user.getUsername(), isOnline , hasUnreadMessages);
            })
            .collect(Collectors.toList());
}

    public void emitRecentChatsUpdate(String userId, List<RecentChatterDto> chats) {
        System.out.println("emitChat" + userId + chats);

         messagingTemplate.convertAndSend("/topic/recent-chats/" + userId, chats);
    
    }
    public void pushRecentChatUpdates(String senderId, String recipientId) {
        // Update Redis recent chat data
        updateRecentChats(senderId, recipientId);
        System.out.println("pushrecentchatupdate" + senderId + recipientId);

        // Push updates to sender if online
        if (redisService.isUserOnline(senderId)) {
            List<RecentChatterDto> senderChats = getRecentChattersWithDetails(senderId);
            emitRecentChatsUpdate(senderId, senderChats);
        }

        // Push updates to recipient if online
        if (redisService.isUserOnline(recipientId)) {
            List<RecentChatterDto> recipientChats = getRecentChattersWithDetails(recipientId);
            emitRecentChatsUpdate(recipientId, recipientChats);
        }
}   

    public boolean hasUnreadMessagesFrom(String senderId, String recipientId) {
    return chatMessageRepository.existsBySenderIdAndRecipientIdAndStatusIn(
        Long.valueOf(senderId),
        Long.valueOf(recipientId),
        List.of(MessageStatus.SENT, MessageStatus.DELIVERED) // assuming DELIVERED still counts as unread
    );
}
    @Transactional  
    public int markMessagesAsRead(Long senderId, Long recipientId) {
        System.out.println("updateingMessaegs" + senderId + recipientId);

        return chatMessageRepository.bulkUpdateStatusBySenderAndRecipient(senderId, recipientId, MessageStatus.READ);
        
    }
    

   public void handleMessageDeliveryStatus( MessageStatusEvent event) {
         Long recipientId = event.getRecipientId();
        Long messageId = event.getMessageId();
        MessageStatus status = event.getStatus();
        ChatMessageEntity message = chatMessageRepository.findById(messageId)
            .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getRecipientId().equals(recipientId)) {
            throw new RuntimeException("Unauthorized to mark this message as delivered");
        }

        message.setStatus(status);
        chatMessageRepository.save(message);

        // Notify sender if needed
        messagingTemplate.convertAndSend(
            "/topic/message-status/" + message.getSenderId(),
            Map.of("messageId", message.getId(), "status", status)
        );
    }
     @Transactional
     public void handleMessageStatusReadUpdate(MessageStatusEvent event) {

        //Long senderId = event.getSenderId();
        long recipientId = event.getRecipientId();
        Long messageId = event.getMessageId();
        MessageStatus status = event.getStatus();

        ChatMessageEntity message = chatMessageRepository.findById(messageId)
            .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!message.getRecipientId().equals(recipientId)) {
            throw new RuntimeException("Unauthorized to mark this message as read");
        }

         message.setStatus(status);
         Long senderId = message.getSenderId();
        // chatMessageRepository.save(message);
        markMessagesAsRead(senderId, recipientId);
        

        // Notify sender if needed
        messagingTemplate.convertAndSend(
            "/topic/message-status/" + message.getSenderId(),
            Map.of("messageId", message.getId(), "status", status)
        );
    }
   
    @Transactional
    public List<ChatMessageEntity> markSentMessagesAsDeliveredAndGetLatestPerSender(Long recipientId) {
        // Step 1: Bulk update status
        chatMessageRepository.bulkMarkSentAsDelivered(recipientId);

        // Step 2: Get latest delivered message per sender
        return chatMessageRepository.findLatestDeliveredMessagesPerSender(recipientId);
    }
   
}
