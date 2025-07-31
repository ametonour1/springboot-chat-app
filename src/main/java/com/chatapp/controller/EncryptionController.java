package com.chatapp.controller;

import com.chatapp.dto.EncryptionKeyDTO;
import com.chatapp.dto.EncryptionStatusDTO;
import com.chatapp.dto.StoreEncryptionKeyDTO;
import com.chatapp.model.EncryptionKey;
import com.chatapp.service.EncryptionKeyService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/encryption")
public class EncryptionController {

    private final EncryptionKeyService encryptionKeyService;

    public EncryptionController(EncryptionKeyService encryptionKeyService) {
        this.encryptionKeyService = encryptionKeyService;
    }

    @GetMapping("/status/{userId}")
    public ResponseEntity<EncryptionStatusDTO> checkUserHasKeys(@PathVariable String userId) {
        boolean hasKeys = encryptionKeyService.userHasKeys(userId);
        EncryptionStatusDTO response = new EncryptionStatusDTO(hasKeys);
        return ResponseEntity.ok(response);
    }

     @PostMapping("/store")
    public ResponseEntity<String> storeEncryptionKey(@RequestBody StoreEncryptionKeyDTO dto) {
         System.out.println("encryption contoller hit");
        EncryptionKey key = new EncryptionKey();
        key.setUserId(dto.getUserId());
        key.setPrivateKeyEncrypted(dto.getPrivateKeyEncrypted());
        key.setPublicKey(dto.getPublicKey());
        key.setSalt(dto.getSalt());
        key.setIv(dto.getIv());
        System.out.println("Private Key: " + dto.getPrivateKeyEncrypted().substring(0, 100) + "...");
        System.out.println("Public Key: " + dto.getPublicKey().substring(0, 100) + "...");

        boolean stored = encryptionKeyService.storeEncryptionKey(key);
        if (!stored) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Encryption key already exists for user.");
        }

        return ResponseEntity.ok("Encryption key stored successfully.");
    }

        @GetMapping("/fetch/{userId}")
         public ResponseEntity<?> getUserKeys(@PathVariable String userId) {
        EncryptionKeyDTO keyDTO = encryptionKeyService.getUserKeyDTO(userId);
        if (keyDTO == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(keyDTO);
    }
}
