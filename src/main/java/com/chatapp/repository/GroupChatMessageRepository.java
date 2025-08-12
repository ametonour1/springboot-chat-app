package com.chatapp.repository;

import com.chatapp.model.GroupChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupChatMessageRepository extends JpaRepository<GroupChatMessage, Long> {
    List<GroupChatMessage> findByGroupChatIdOrderByTimestampDesc(Long groupChatId);
    List<GroupChatMessage> findByGroupChatIdAndKeyVersion(Long groupChatId, Integer keyVersion);
}