package com.chatapp.repository;

import com.chatapp.model.EncryptionKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EncryptionKeyRepository extends JpaRepository<EncryptionKey, String> {
    boolean existsByUserId(String userId);
}