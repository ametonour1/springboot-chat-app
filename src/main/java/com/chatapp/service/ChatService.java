package com.chatapp.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.chatapp.dto.ChatMessage;
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

         saveMessage(entity);

        if (redisService.isUserOnline(userId)) {
            messagingTemplate.convertAndSend("/topic/messages/" + userId, message);
        } else {
            // handle offline user case here (store message or log)
            System.out.println("User " + userId + " is offline. saved to DB.");
        }
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
            .map(user -> new RecentChatterDto(user.getId().toString(), user.getUsername()))
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

   
}
