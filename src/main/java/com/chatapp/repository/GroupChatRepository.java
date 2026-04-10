package com.chatapp.repository;
import com.chatapp.model.GroupChat;

import io.lettuce.core.dynamic.annotation.Param;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface GroupChatRepository extends JpaRepository<GroupChat, Long> {

    // Fetch only the version number (lighter than fetching the whole object)
    @Query("SELECT g.currentKeyVersion FROM GroupChat g WHERE g.id = :id")
    Integer findKeyVersionById(@Param("id") Long id);

    // Atomic increment for rotation
    @Modifying
    @Transactional
    @Query("UPDATE GroupChat g SET g.currentKeyVersion = g.currentKeyVersion + 1 WHERE g.id = :id")
    void incrementKeyVersion(@Param("id") Long id);
}