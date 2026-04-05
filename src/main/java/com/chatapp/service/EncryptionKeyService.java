
package com.chatapp.service;

import com.chatapp.dto.EncryptionKeyDTO;
import com.chatapp.model.EncryptionKey;
import com.chatapp.repository.EncryptionKeyRepository;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.SecretKey;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

import java.util.Base64;

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

      public static SecretKey generateAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        return keyGen.generateKey();
    }

    // Encrypt AES key with RSA public key using SHA-1 (compatible with Web Crypto)
    public static byte[] encryptWithPublicKey(byte[] data, PublicKey publicKey) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    // Load public key from Base64 string
    public static PublicKey loadPublicKeyFromString(String keyStr) throws GeneralSecurityException {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(keyStr);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
}
