package com.chatapp.service;

import java.util.ArrayList;
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
import com.chatapp.service.GroupChatService;

@Service
public class ChatService {

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisService redisService;
    private final ChatMessageRepository chatMessageRepository;
    private final UserService userService;
    private final GroupChatService groupChatService;

    public ChatService(SimpMessagingTemplate messagingTemplate, RedisService redisService, ChatMessageRepository chatMessageRepository, UserService userService, GroupChatService groupChatService
) {
        this.messagingTemplate = messagingTemplate;
        this.redisService = redisService;
        this.chatMessageRepository = chatMessageRepository;
        this.userService = userService;
        this.groupChatService = groupChatService;
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

    public void updateRecentChats(String userId, String chatterId, boolean isGroup) {
        
         String prefixedChatterId = isGroup ? "group_" + chatterId : "user_" + chatterId;

        redisService.addRecentChatter(userId, prefixedChatterId);

        // If group, you might skip reverse because groups don’t "view" chats
        if (!isGroup) {
            String prefixedUserId = "user_" + userId;
            redisService.addRecentChatter(chatterId, prefixedUserId);
        }


    }

    public void returneRecentChats(String userId) {
        
      redisService.getRecentChatters(userId);
    }

 public List<RecentChatterDto> getRecentChattersWithDetails(String userId) {
    System.out.println("Fetching recent chatters for userId: " + userId);
    
    List<String> chatterIds = redisService.getRecentChatters(userId);
    System.out.println("chatterIds from Redis: " + chatterIds);

    // Separate user IDs and group IDs
    List<String> userIds = new ArrayList<>();
    List<String> groupIds = new ArrayList<>();

   for (String id : chatterIds) {
    if (id.startsWith("group_")) {
        groupIds.add(id.replace("group_", ""));
    } else if (id.startsWith("user_")) {
        userIds.add(id.replace("user_", ""));  // Remove the "user_" prefix here
    } else {
        userIds.add(id); // In case there are raw ids without prefix
    }
}

    System.out.println("User IDs extracted: " + userIds);
    System.out.println("Group IDs extracted: " + groupIds);

    // Fetch data from DB
    List<User> users = userService.getUsersByIds(userIds);
    System.out.println("Users fetched from DB: " + users.size());

    List<GroupChat> groups = groupChatService.getGroupsByIds(groupIds);
    System.out.println("Groups fetched from DB: " + groups.size());

    // Map for quick lookup
    Map<String, User> userMap = users.stream()
        .collect(Collectors.toMap(u -> u.getId().toString(), u -> u));
    System.out.println("User map keys: " + userMap.keySet());

    Map<String, GroupChat> groupMap = groups.stream()
        .collect(Collectors.toMap(g -> "group_" + g.getId(), g -> g));
    System.out.println("Group map keys: " + groupMap.keySet());

    // Build final list preserving Redis order
    List<RecentChatterDto> results = chatterIds.stream()
    .map(id -> {
        System.out.println("Processing id: " + id);
        if (id.startsWith("group_")) {
            String cleanGroupId = id.replace("group_", "");
            GroupChat group = groupMap.get(cleanGroupId);
            System.out.println("Looking up groupMap with: " + cleanGroupId + " -> " + group);
            if (group != null) {
                boolean hasUnreadMessages = hasUnreadMessagesFrom(cleanGroupId, userId);
                return new RecentChatterDto(
                    cleanGroupId,
                    group.getName(),
                    false,
                    hasUnreadMessages,
                    null,
                    "GROUP"
                );
            }
        } else if (id.startsWith("user_")) {
            String cleanUserId = id.replace("user_", "");
            System.out.println("Looking up userMap with: " + cleanUserId);
            User user = userMap.get(cleanUserId);
            System.out.println("User found: " + user);
            if (user != null) {
                boolean isOnline = redisService.isUserOnline(cleanUserId);
                boolean hasUnreadMessages = hasUnreadMessagesFrom(cleanUserId, userId);
                String publicKey = redisService.getUserPublicKey(cleanUserId);
                return new RecentChatterDto(
                    cleanUserId,
                    user.getUsername(),
                    isOnline,
                    hasUnreadMessages,
                    publicKey,
                    "USER"
                );
            }
        } else {
            System.out.println("Unexpected id format: " + id);
        }
        System.out.println("No user or group found for id: " + id);
        return null;
    })
    .filter(Objects::nonNull)
    .collect(Collectors.toList());

    System.out.println("Final RecentChatterDto list size: " + results.size());
    return results;
}



    public void emitRecentChatsUpdate(String userId, List<RecentChatterDto> chats) {
        System.out.println("emitChat" + userId + chats);

         messagingTemplate.convertAndSend("/topic/recent-chats/" + userId, chats);
    
    }
    public void pushRecentChatUpdates(String senderId, String recipientId, boolean isGroup) {
        // Update Redis recent chat data
        updateRecentChats(senderId, recipientId, isGroup);
        System.out.println("pushrecentchatupdate" + senderId + recipientId);
        System.out.println("pushRecentChatUpdates called with senderId: " + senderId + ", recipientId: " + recipientId + ", isGroup: " + isGroup);


        // Push updates to sender if online
        if (redisService.isUserOnline(senderId)) {
            List<RecentChatterDto> senderChats = getRecentChattersWithDetails(senderId);
            emitRecentChatsUpdate(senderId, senderChats);
        }

         // Push updates to recipient if online (only if it's a private chat)
        if (!isGroup && redisService.isUserOnline(recipientId)) {
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
