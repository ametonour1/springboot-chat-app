package com.chatapp.model;

import javax.persistence.*;

@Entity
@Table(name = "group_keys")
public class GroupKeyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "encrypted_key", nullable = false, columnDefinition = "TEXT")
    private String encryptedKey;

    @Column(name = "key_version", nullable = false)
    private Integer keyVersion = 1; // default to version 1

    @Column(name = "group_chat_id", nullable = false)
    private Long groupChatId;
    
    @Column(name = "iv", nullable = false, columnDefinition = "TEXT")
    private String iv; // New IV field for symmetric encryption

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



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

    public Long getGroupChatId() {
    return groupChatId;
}

    public void setGroupChatId(Long groupChatId) {
        this.groupChatId = groupChatId;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }
}
