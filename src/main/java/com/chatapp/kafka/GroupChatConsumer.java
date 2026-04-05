package com.chatapp.kafka;

import com.chatapp.dto.GroupChatMessageRequest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.chatapp.service.GroupChatService;

@Service
public class GroupChatConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    private final GroupChatService groupChatService;

    public GroupChatConsumer(SimpMessagingTemplate messagingTemplate, GroupChatService groupChatService) {
        this.messagingTemplate = messagingTemplate;
        this.groupChatService = groupChatService;
    }

    @KafkaListener(
        topics = "group-chat-messages", 
        groupId = "group-chat-consumers", 
        containerFactory = "groupChatKafkaListenerContainerFactory"
    )
    public void consume(GroupChatMessageRequest message) {
        groupChatService.sendGroupChatMessage(message); 
        System.out.println("Broadcasted group message to group: " + message.getGroupChatId());
    }
}
