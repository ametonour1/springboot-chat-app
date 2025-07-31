
package com.chatapp.service;

import com.chatapp.dto.EncryptionKeyDTO;
import com.chatapp.model.EncryptionKey;
import com.chatapp.repository.EncryptionKeyRepository;
import org.springframework.stereotype.Service;

@Service
public class EncryptionKeyService {

    private final EncryptionKeyRepository encryptionKeyRepository;

    public EncryptionKeyService(EncryptionKeyRepository encryptionKeyRepository) {
        this.encryptionKeyRepository = encryptionKeyRepository;
    }

    public boolean userHasKeys(String userId) {
        return encryptionKeyRepository.existsByUserId(userId);
    }

      public boolean storeEncryptionKey(EncryptionKey key) {
        if (userHasKeys(key.getUserId())) {
            return false;
        }
        encryptionKeyRepository.save(key);
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
