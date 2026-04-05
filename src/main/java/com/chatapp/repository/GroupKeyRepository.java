package com.chatapp.repository;


import com.chatapp.model.GroupKeyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupKeyRepository extends JpaRepository<GroupKeyEntity, Long> {

    List<GroupKeyEntity> findByGroupChatId(Long groupChatId);

    Optional<GroupKeyEntity> findByGroupChatIdAndUserId(Long groupChatId, Long userId);

    List<GroupKeyEntity> findByGroupChatIdAndKeyVersion(Long groupChatId, Integer keyVersion);

    void deleteByGroupChatIdAndUserId(Long groupChatId, Long userId);

    void deleteByGroupChatId(Long groupChatId);
}