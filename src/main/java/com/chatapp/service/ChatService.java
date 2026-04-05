package com.chatapp.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;

import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;


import com.chatapp.dto.ChatMessage;
import com.chatapp.dto.MessageStatusEvent;
import com.chatapp.dto.RecentChatterDto;
import com.chatapp.model.ChatMessageEntity;
import com.chatapp.model.GroupChat;
import com.chatapp.model.MessageStatus;
import com.chatapp.model.User;
import com.chatapp.repository.ChatMessageRepository;
import com.chatapp.service.RecentChatterService;


@Service
public class ChatService {

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisService redisService;
    private final ChatMessageRepository chatMessageRepository;
    private final UserService userService;

    private final RecentChatterService recentChatterService;

    public ChatService(SimpMessagingTemplate messagingTemplate, RedisService redisService, ChatMessageRepository chatMessageRepository, UserService userService, RecentChatterService recentChatterService
) {
        this.messagingTemplate = messagingTemplate;
        this.redisService = redisService;
        this.chatMessageRepository = chatMessageRepository;
        this.userService = userService;
        this.recentChatterService = recentChatterService;
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
                false ,
                entity.getEncryptedAESKeyForSender(),
                entity.getEncryptedAESKeyForRecipient(),
                entity.getIv()
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
                true,  
                entity.getEncryptedAESKeyForSender(),
                entity.getEncryptedAESKeyForRecipient(),
                entity.getIv()
            );
            messagingTemplate.convertAndSend("/topic/messages/" + message.getSenderId().toString(), msgToSender);

            redisService.addMessageToCache(entity.getSenderId(),entity.getRecipientId(),entity);
    }

    private ChatMessageEntity mapDtoToEntity(ChatMessage dto) {
    ChatMessageEntity entity = new ChatMessageEntity();
    entity.setSenderId(dto.getSenderId());
    entity.setRecipientId(dto.getRecipientId());
    entity.setContent(dto.getContent());
    entity.setStatus(MessageStatus.SENT); // or default status

    entity.setEncryptedAESKeyForSender(dto.getEncryptedAESKeyForSender());
    entity.setEncryptedAESKeyForRecipient(dto.getEncryptedAESKeyForRecipient());
    entity.setIv(dto.getIv());

    return entity;
}

    public ChatMessageEntity saveMessage(ChatMessageEntity message) {
        return chatMessageRepository.save(message);
    }

    // public void updateRecentChats(String userId, String chatterId, boolean isGroup) {
        
    //      String prefixedChatterId = isGroup ? "group_" + chatterId : "user_" + chatterId;

    //     redisService.addRecentChatter(userId, prefixedChatterId);

    //     // If group, you might skip reverse because groups don’t "view" chats
    //     if (!isGroup) {
    //         String prefixedUserId = "user_" + userId;
    //         redisService.addRecentChatter(chatterId, prefixedUserId);
    //     }


    // }

    public void returneRecentChats(String userId) {
        
      redisService.getRecentChatters(userId);
    }

 


    // public void emitRecentChatsUpdate(String userId, List<RecentChatterDto> chats) {
    //     System.out.println("emitChat" + userId + chats);

    //      messagingTemplate.convertAndSend("/topic/recent-chats/" + userId, chats);
    
    // }
    public void pushRecentChatUpdates(String senderId, String recipientId, boolean isGroup) {
        // Update Redis recent chat data
        recentChatterService.updateRecentChats(senderId, recipientId);
        System.out.println("pushrecentchatupdate" + senderId + recipientId);
        System.out.println("pushRecentChatUpdates called with senderId: " + senderId + ", recipientId: " + recipientId + ", isGroup: " + isGroup);


        // Push updates to sender if online
        if (redisService.isUserOnline(senderId)) {
            List<RecentChatterDto> senderChats = recentChatterService.getRecentChattersWithDetails(senderId);
            recentChatterService.emitRecentChatsUpdate(senderId, senderChats);
        }

         // Push updates to recipient if online (only if it's a private chat)
        if (!isGroup && redisService.isUserOnline(recipientId)) {
            List<RecentChatterDto> recipientChats = recentChatterService.getRecentChattersWithDetails(recipientId);
            recentChatterService.emitRecentChatsUpdate(recipientId, recipientChats);
        }
}   

    public boolean hasUnreadMessagesFrom(String senderId, String recipientId) {
    return chatMessageRepository.existsBySenderIdAndRecipientIdAndStatusIn(
        Long.valueOf(senderId),
        Long.valueOf(recipientId),
        List.of(MessageStatus.SENT, MessageStatus.DELIVERED) // assuming DELIVERED still counts as unread
    );
}

// public Map<String, Boolean> getUsersUnreadStatus(List<String> userIds, String recipientId) {
//     if (userIds.isEmpty()) {
//         return Collections.emptyMap();
//     }

//     List<Long> senderIds = userIds.stream()
//                                   .map(Long::valueOf)
//                                   .collect(Collectors.toList());
//     Long recipient = Long.valueOf(recipientId);
//     List<MessageStatus> unreadStatuses = List.of(MessageStatus.SENT, MessageStatus.DELIVERED);

//     List<ChatMessageRepository.SenderUnreadStatus> unreadStatusesList =
//         chatMessageRepository.findUnreadStatusBySenderIdsAndRecipientId(senderIds, recipient, unreadStatuses);

//     Map<String, Boolean> unreadMap = new HashMap<>();
//     // Initialize all to false (no unread)
//     userIds.forEach(id -> unreadMap.put(id, false));

//     // Mark those senders who have unread messages
//     for (ChatMessageRepository.SenderUnreadStatus status : unreadStatusesList) {
//         unreadMap.put(status.getSenderId().toString(), status.getHasUnread());
//     }

//     return unreadMap;
// }
    
   
    @Transactional  
    public int markMessagesAsRead(Long senderId, Long recipientId) {
         int count = chatMessageRepository.countUnreadMessages(senderId, recipientId, MessageStatus.READ);
        if (count == 0) {
            System.out.println("retuning no messes to update for" + senderId + recipientId);

            return 0; // Nothing to do
        }

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
        redisService.updateCachedMessageStatus(message);
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
        redisService.updateCachedMessageStatus(message);
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

    public List<ChatMessageEntity> getMessagesBetweenUsers(Long userId1, Long userId2, int skip, int limit) {
   
     return chatMessageRepository.findMessagesBetweenUsers(userId1, userId2, skip, limit);
}
   
}
