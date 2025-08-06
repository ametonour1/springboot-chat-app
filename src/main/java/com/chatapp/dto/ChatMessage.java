package com.chatapp.dto;

import com.chatapp.model.MessageStatus;

public class ChatMessage {
    private Long senderId;
    private Long recipientId;
    private String content;
    private long timestamp;
    private MessageStatus status;
    private boolean me; 
    private Long id;
    private String encryptedAESKeyForSender;
    private String encryptedAESKeyForRecipient;
    private String iv; 

    // Constructors
    public ChatMessage() {}

    public ChatMessage(Long id, Long senderId, Long recipientId, String content, long timestamp, MessageStatus status, boolean me,String encryptedAESKeyForSender, String encryptedAESKeyForRecipient,
                      String iv) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.content = content;
        this.timestamp = timestamp;
        this.status = status;
        this.me = me;
        this.id = id;
        this.encryptedAESKeyForSender = encryptedAESKeyForSender;
        this.encryptedAESKeyForRecipient = encryptedAESKeyForRecipient;
        this.iv = iv;
    }

    // Getters and setters
     public Long getId() { return id; }                      // <- Getter
    public void setId(Long id) { this.id = id; }            // <- Setter


    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }


    public MessageStatus getStatus() { return status; }
    public void setStatus(MessageStatus status) { this.status = status; }
       
    public boolean isMe() { return me; }
    public void setMe(boolean me) { this.me = me; }

     public String getEncryptedAESKeyForSender() {
        return encryptedAESKeyForSender;
    }

    public void setEncryptedAESKeyForSender(String encryptedAESKeyForSender) {
        this.encryptedAESKeyForSender = encryptedAESKeyForSender;
    }

    public String getEncryptedAESKeyForRecipient() {
        return encryptedAESKeyForRecipient;
    }

    public void setEncryptedAESKeyForRecipient(String encryptedAESKeyForRecipient) {
        this.encryptedAESKeyForRecipient = encryptedAESKeyForRecipient;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }
}
