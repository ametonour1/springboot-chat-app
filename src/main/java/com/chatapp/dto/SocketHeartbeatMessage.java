package com.chatapp.dto;

public class SocketHeartbeatMessage {
    private String userId;

    public SocketHeartbeatMessage() {
    }

    public SocketHeartbeatMessage(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
