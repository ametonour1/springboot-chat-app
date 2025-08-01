
package com.chatapp.service;

import com.chatapp.dto.EncryptionKeyDTO;
import com.chatapp.model.EncryptionKey;
import com.chatapp.repository.EncryptionKeyRepository;
import org.springframework.stereotype.Service;
import com.chatapp.service.RedisService;

@Service
public class EncryptionKeyService {

    private final EncryptionKeyRepository encryptionKeyRepository;
    private final RedisService redisService;

    public EncryptionKeyService(EncryptionKeyRepository encryptionKeyRepository, RedisService redisService) {
        this.encryptionKeyRepository = encryptionKeyRepository;
        this.redisService = redisService;
    }

    public boolean userHasKeys(String userId) {
        return encryptionKeyRepository.existsByUserId(userId);
    }

      public boolean storeEncryptionKey(EncryptionKey key) {
        if (userHasKeys(key.getUserId())) {
            return false;
        }
        encryptionKeyRepository.save(key);
        redisService.cacheUserPublicKey(key.getUserId(), key.getPublicKey());
        return true;
    }


    public EncryptionKey getKeyByUserId(String userId) {
        return encryptionKeyRepository.findById(userId).orElse(null);
    }


    public EncryptionKeyDTO getUserKeyDTO(String userId) {
        EncryptionKey key = getKeyByUserId(userId);
        if (key == null) {
            return null;
        }

        return new EncryptionKeyDTO(
            key.getUserId(),
            key.getPrivateKeyEncrypted(),
            key.getPublicKey(),
            key.getSalt(),
            key.getIv()
        );
    }
}
