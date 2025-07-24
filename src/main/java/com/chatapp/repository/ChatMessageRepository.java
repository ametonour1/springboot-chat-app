package com.chatapp.repository;

import com.chatapp.model.ChatMessageEntity;
import com.chatapp.model.MessageStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    
    // Fetch messages between two users (both directions)
    List<ChatMessageEntity> findBySenderIdAndRecipientIdOrSenderIdAndRecipientId(
        Long senderId1, Long recipientId1,
        Long senderId2, Long recipientId2
    );

    // Fetch all messages for a recipient
    List<ChatMessageEntity> findByRecipientId(Long recipientId);

    // Optional: Unread messages
    List<ChatMessageEntity> findByRecipientIdAndStatus(Long recipientId, MessageStatus status);

    boolean existsByRecipientIdAndStatusIn(Long recipientId, Iterable<MessageStatus> statuses);

    boolean existsBySenderIdAndRecipientIdAndStatusIn(Long senderId, Long recipientId, Iterable<MessageStatus> statuses);

}