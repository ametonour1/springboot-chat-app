package com.chatapp.dto;

import java.time.LocalDateTime;

public class ChatMessage {
    private String senderUsername;
    private String recipientUsername;
    private String content;
    private long timestamp;

    // Constructors
    public ChatMessage() {}
    
    public ChatMessage(String senderUsername, String recipientUsername, String content, long timestamp) {
        this.senderUsername = senderUsername;
        this.recipientUsername = recipientUsername;
        this.content = content;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getSenderUsername() { return senderUsername; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }

    public String getRecipientUsername() { return recipientUsername; }
    public void setRecipientUsername(String recipientUsername) { this.recipientUsername = recipientUsername; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
