package com.chatapp.dto;

public class ChatMessage {
    private Long senderId;
    private Long recipientId;
    private String content;
    private long timestamp;

    // Constructors
    public ChatMessage() {}

    public ChatMessage(Long senderId, Long recipientId, String content, long timestamp) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public Long getRecipientId() { return recipientId; }
    public void setRecipientId(Long recipientId) { this.recipientId = recipientId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
