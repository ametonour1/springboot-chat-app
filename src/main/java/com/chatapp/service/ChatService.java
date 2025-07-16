package com.chatapp.service;

import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.chatapp.dto.ChatMessage;
import com.chatapp.model.ChatMessageEntity;
import com.chatapp.model.MessageStatus;
import com.chatapp.repository.ChatMessageRepository;

@Service
public class ChatService {

    private final SimpMessagingTemplate messagingTemplate;
    private final RedisService redisService;
    private final ChatMessageRepository chatMessageRepository;

    public ChatService(SimpMessagingTemplate messagingTemplate, RedisService redisService, ChatMessageRepository chatMessageRepository) {
        this.messagingTemplate = messagingTemplate;
        this.redisService = redisService;
        this.chatMessageRepository = chatMessageRepository;
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

   
}
