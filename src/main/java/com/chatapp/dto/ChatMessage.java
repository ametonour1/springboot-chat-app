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

    // Constructors
    public ChatMessage() {}

    public ChatMessage(Long id, Long senderId, Long recipientId, String content, long timestamp, MessageStatus status, boolean me) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.content = content;
        this.timestamp = timestamp;
        this.status = status;
        this.me = me;
        this.id = id;
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
}
