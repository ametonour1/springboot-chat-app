package com.chatapp.repository;


import com.chatapp.model.GroupChatMessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupChatMessageStatusRepository extends JpaRepository<GroupChatMessageStatus, Long> {
    List<GroupChatMessageStatus> findByMessageId(Long messageId);
    List<GroupChatMessageStatus> findByUserId(Long userId);
}