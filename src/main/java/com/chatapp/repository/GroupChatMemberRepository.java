package com.chatapp.repository;

import com.chatapp.model.GroupChatMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupChatMemberRepository extends JpaRepository<GroupChatMember, Long> {
    List<GroupChatMember> findByGroupChatId(Long groupChatId);
    List<GroupChatMember> findByUserId(Long userId);
    boolean existsByGroupChatIdAndUserId(Long groupChatId, Long userId);
}