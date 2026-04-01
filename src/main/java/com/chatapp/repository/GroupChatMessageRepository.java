package com.chatapp.repository;

import com.chatapp.model.GroupChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface GroupChatMessageRepository extends JpaRepository<GroupChatMessage, Long> {
    List<GroupChatMessage> findByGroupChatIdOrderByTimestampDesc(Long groupChatId);
    List<GroupChatMessage> findByGroupChatIdAndKeyVersion(Long groupChatId, Integer keyVersion);

    @Query(value = """
    SELECT * FROM (
        SELECT * FROM group_chat_messages 
        WHERE group_chat_id = :groupId AND timestamp < :before 
        ORDER BY timestamp DESC 
        LIMIT :limit
    ) AS subquery 
    ORDER BY timestamp ASC
    """, nativeQuery = true)
List<GroupChatMessage> findHistoricalGaps(
    @Param("groupId") Long groupId, 
    @Param("before") LocalDateTime before, 
    @Param("limit") int limit
);

@Query(value = """
    SELECT * FROM (
        SELECT * FROM group_chat_messages 
        WHERE group_chat_id = :groupId AND timestamp > :after 
        ORDER BY timestamp DESC 
        LIMIT :limit
    ) AS subquery 
    ORDER BY timestamp ASC
    """, nativeQuery = true)
List<GroupChatMessage> findLatestGaps(
    @Param("groupId") Long groupId, 
    @Param("after") LocalDateTime after, 
    @Param("limit") int limit
);
}

