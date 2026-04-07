package com.chatapp.repository;

import com.chatapp.model.GroupMessageStatus;
import com.chatapp.model.GroupMessageStatusId;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface GroupMessageStatusRepository extends JpaRepository<GroupMessageStatus, GroupMessageStatusId> {

    /**
     * Native Postgres UPSERT query. 
     * Extremely fast because it happens purely on the DB level in one operation.
     */
    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO group_message_status (group_chat_id, user_id, last_read_message_id)
        VALUES (:groupChatId, :userId, :lastReadMessageId)
        ON CONFLICT (group_chat_id, user_id) 
        DO UPDATE SET last_read_message_id = EXCLUDED.last_read_message_id
    """, nativeQuery = true)
    void upsertUserReadStatus(
        @Param("groupChatId") Long groupChatId,
        @Param("userId") Long userId,
        @Param("lastReadMessageId") Long lastReadMessageId
    );

    List<GroupMessageStatus> findByGroupChatId(Long groupChatId);
}