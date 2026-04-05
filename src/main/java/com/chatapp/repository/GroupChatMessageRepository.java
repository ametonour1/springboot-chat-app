package com.chatapp.repository;

import com.chatapp.model.GroupChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;


public interface GroupChatMessageRepository extends JpaRepository<GroupChatMessage, Long> {
    List<GroupChatMessage> findByGroupChatIdOrderByTimestampDesc(Long groupChatId);
    List<GroupChatMessage> findByGroupChatIdAndKeyVersion(Long groupChatId, Integer keyVersion);
    List<GroupChatMessage> findFirst20ByGroupChatIdAndTimestampLessThanOrderByTimestampDesc(
        Long groupChatId, 
        LocalDateTime timestamp
    );

    List<GroupChatMessage> findByGroupChatIdAndTimestampLessThanOrderByTimestampDesc(
        Long groupChatId, 
        LocalDateTime timestamp,
        Pageable pageable
    );

@Query(value = """
    SELECT * FROM group_chat_messages 
    WHERE group_chat_id = :groupId 
      AND timestamp < CAST(:before AS TIMESTAMP) 
    ORDER BY timestamp DESC 
    LIMIT :limit
    """, nativeQuery = true)
List<GroupChatMessage> findHistoricalGaps(
    @Param("groupId") Long groupId, 
    @Param("before") String before, // <-- Changed to String!
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

@Query(value = """
    SELECT * FROM (
        SELECT * FROM group_chat_messages 
        WHERE group_chat_id = :groupId 
        ORDER BY timestamp DESC 
        LIMIT :limit
    ) AS subquery 
    ORDER BY timestamp ASC
    """, nativeQuery = true)
List<GroupChatMessage> findInitialMessages(
    @Param("groupId") Long groupId, 
    @Param("limit") int limit
);
}

