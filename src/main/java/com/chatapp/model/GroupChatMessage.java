package com.chatapp.model;


import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_chat_messages")
public class GroupChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_chat_id", nullable = false)
    private Long groupChatId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "iv", nullable = false)
    private String iv;

    @Column(name = "encrypted_aes_keys", columnDefinition = "JSONB")
    private String encryptedAESKeysJson;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "key_version", nullable = false)
    private Integer keyVersion = 1;  // default to version 1

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGroupChatId() {
        return groupChatId;
    }

    public void setGroupChatId(Long groupChatId) {
        this.groupChatId = groupChatId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public String getEncryptedAESKeysJson() {
        return encryptedAESKeysJson;
    }

    public void setEncryptedAESKeysJson(String encryptedAESKeysJson) {
        this.encryptedAESKeysJson = encryptedAESKeysJson;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getKeyVersion() {
    return keyVersion;
}

    public void setKeyVersion(Integer keyVersion) {
        this.keyVersion = keyVersion;
    }
}
