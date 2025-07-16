package com.chatapp.service;

import java.util.List;
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

    return users.stream()
        .map(user -> new RecentChatterDto(user.getId().toString(), user.getUsername()))
        .collect(Collectors.toList());
}

   
}
