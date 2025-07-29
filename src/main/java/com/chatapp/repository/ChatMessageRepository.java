package com.chatapp.repository;

import com.chatapp.model.ChatMessageEntity;
import com.chatapp.model.MessageStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import javax.transaction.Transactional;

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


    @Modifying
@Transactional
@Query(value = """
    UPDATE chat_messages
    SET status = 'DELIVERED'
    WHERE recipient_id = :recipientId AND status = 'SENT'
""", nativeQuery = true)
void bulkMarkSentAsDelivered(@Param("recipientId") Long recipientId);


@Query(value = """
    SELECT *
    FROM chat_messages m
    WHERE m.recipient_id = :recipientId
      AND m.status = 'DELIVERED'
      AND m.timestamp = (
        SELECT MAX(m2.timestamp)
        FROM chat_messages m2
        WHERE m2.sender_id = m.sender_id
          AND m2.recipient_id = :recipientId
          AND m2.status = 'DELIVERED'
      )
""", nativeQuery = true)
List<ChatMessageEntity> findLatestDeliveredMessagesPerSender(@Param("recipientId") Long recipientId);


@Modifying
@Query("UPDATE ChatMessageEntity m SET m.status = :status WHERE m.senderId = :senderId AND m.recipientId = :recipientId AND m.status <> :status")
int bulkUpdateStatusBySenderAndRecipient(@Param("senderId") Long senderId, 
                                        @Param("recipientId") Long recipientId,
                                        @Param("status") MessageStatus status);
 
         
                                        

@Query("SELECT COUNT(m) FROM ChatMessageEntity m WHERE m.senderId = :senderId AND m.recipientId = :recipientId AND m.status <> :status")
int countUnreadMessages(Long senderId, Long recipientId, MessageStatus status);

@Query("""
    SELECT m FROM ChatMessageEntity m
    WHERE 
        (m.senderId = :userId1 AND m.recipientId = :userId2)
        OR 
        (m.senderId = :userId2 AND m.recipientId = :userId1)
    ORDER BY m.timestamp DESC
""")
Page<ChatMessageEntity> findMessagesByParticipants(
    @Param("userId1") Long userId1,
    @Param("userId2") Long userId2,
    Pageable pageable
);
}

