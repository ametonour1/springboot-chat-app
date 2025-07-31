// EncryptionKeyDTO.java
package com.chatapp.dto;

public class EncryptionKeyDTO {
    private String userId;
    private String privateKeyEncrypted;
    private String publicKey;
    private String salt;
    private String iv;

    // Constructors
    public EncryptionKeyDTO() {}

    public EncryptionKeyDTO(String userId, String privateKeyEncrypted, String publicKey, String salt, String iv) {
        this.userId = userId;
        this.privateKeyEncrypted = privateKeyEncrypted;
        this.publicKey = publicKey;
        this.salt = salt;
        this.iv = iv;
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getPrivateKeyEncrypted() { return privateKeyEncrypted; }
    public void setPrivateKeyEncrypted(String privateKeyEncrypted) { this.privateKeyEncrypted = privateKeyEncrypted; }

    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public String getIv() { return iv; }
    public void setIv(String iv) { this.iv = iv; }
}
