package com.chatapp.dto;

public class SocketHandshakeMessage {
    private String userId;

    public SocketHandshakeMessage() {}

    public SocketHandshakeMessage(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
