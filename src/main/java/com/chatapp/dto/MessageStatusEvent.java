package com.chatapp.dto;


import com.chatapp.model.MessageStatus;

import java.io.Serializable;

public class MessageStatusEvent implements Serializable {

    private Long messageId;           // ID of the message whose status is changing
    private Long recipientId;         // ID of the user receiving the message
    private Long senderId;            // ID of the sender (who gets notified)
    private MessageStatus status;     // New status: SENT, DELIVERED, READ

    public MessageStatusEvent() {
    }

    public MessageStatusEvent(Long messageId, Long recipientId, Long senderId, MessageStatus status) {
        this.messageId = messageId;
        this.recipientId = recipientId;
        this.senderId = senderId;
        this.status = status;
    }

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "MessageStatusEvent{" +
            "messageId=" + messageId +
            ", recipientId=" + recipientId +
            ", senderId=" + senderId +
            ", status=" + status +
            '}';
    }
}
