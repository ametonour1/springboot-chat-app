package com.chatapp.repository;

import com.chatapp.dto.UserSummaryDTO;
import com.chatapp.model.GroupChatMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

// Check if user is Admin
    boolean existsByGroupChatIdAndUserIdAndIsAdminTrue(Long groupChatId, Long userId);

    // Delete the kicked user
    @Modifying
    @Query("DELETE FROM GroupChatMember gcm WHERE gcm.groupChatId = :groupId AND gcm.userId = :userId")
    void deleteByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") Long userId);

    // Get all userIds for Redis sync
    @Query("SELECT gcm.userId FROM GroupChatMember gcm WHERE gcm.groupChatId = :groupId")
    List<Long> findAllUserIdsByGroupId(@Param("groupId") Long groupId);
}