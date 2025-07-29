package com.chatapp.dto;

public class CachedMessagesRequest {
    private Long senderId;
    private Long recipientId;
    private Integer offset;  // how many messages to skip (e.g. 25)
    private Integer limit;   // how many messages to return (e.g. 10)

    public CachedMessagesRequest() {
    }

    public CachedMessagesRequest(Long senderId, Long recipientId, Integer offset, Integer limit) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.offset = offset;
        this.limit = limit;
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

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        return "CachedMessagesRequest{" +
                "senderId=" + senderId +
                ", recipientId=" + recipientId +
                ", offset=" + offset +
                ", limit=" + limit +
                '}';
    }
}
