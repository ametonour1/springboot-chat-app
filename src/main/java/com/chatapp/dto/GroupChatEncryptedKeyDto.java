package com.chatapp.dto;

public class GroupChatEncryptedKeyDto {

    private Long userId;
    private String encryptedKey;
    private Integer keyVersion;

    public GroupChatEncryptedKeyDto() {}

    public GroupChatEncryptedKeyDto(Long userId, String encryptedKey, Integer keyVersion) {
        this.userId = userId;
        this.encryptedKey = encryptedKey;
        this.keyVersion = keyVersion;
    }

    // Getters and setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEncryptedKey() {
        return encryptedKey;
    }

    public void setEncryptedKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

    public Integer getKeyVersion() {
        return keyVersion;
    }

    public void setKeyVersion(Integer keyVersion) {
        this.keyVersion = keyVersion;
    }
}
