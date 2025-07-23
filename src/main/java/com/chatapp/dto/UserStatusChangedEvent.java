package com.chatapp.dto;

public class UserStatusChangedEvent {
    private String userId;
    private boolean isOnline;

    public UserStatusChangedEvent() {
    }

    public UserStatusChangedEvent(String userId, boolean isOnline) {
        this.userId = userId;
        this.isOnline = isOnline;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}
