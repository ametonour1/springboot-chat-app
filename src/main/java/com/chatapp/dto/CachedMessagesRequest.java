package com.chatapp.dto;


public class CachedMessagesRequest {
    private Long senderId;
    private Long recipientId;

    public CachedMessagesRequest() {
    }

    public CachedMessagesRequest(Long senderId, Long recipientId) {
        this.senderId = senderId;
        this.recipientId = recipientId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    @Override
    public String toString() {
        return "CachedMessagesRequest{" +
                "senderId=" + senderId +
                ", recipientId=" + recipientId +
                '}';
    }
}
