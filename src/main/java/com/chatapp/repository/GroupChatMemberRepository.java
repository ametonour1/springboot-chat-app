package com.chatapp.repository;

import com.chatapp.dto.UserSummaryDTO;
import com.chatapp.model.GroupChatMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GroupChatMemberRepository extends JpaRepository<GroupChatMember, Long> {
    List<GroupChatMember> findByGroupChatId(Long groupChatId);
    List<GroupChatMember> findByUserId(Long userId);
    boolean existsByGroupChatIdAndUserId(Long groupChatId, Long userId);

@Query("SELECT new com.chatapp.dto.UserSummaryDTO(u.id, u.username, gcm.isAdmin) " +
       "FROM GroupChatMember gcm " +
       "JOIN User u ON gcm.userId = u.id " + 
       "WHERE gcm.groupChatId = :groupChatId")
List<UserSummaryDTO> findMembersByGroupId(@Param("groupChatId") Long groupChatId);
}