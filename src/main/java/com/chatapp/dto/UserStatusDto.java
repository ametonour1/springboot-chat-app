package com.chatapp.dto;

public class UserStatusDto {
    private String userId;
    private boolean isOnline;

    public UserStatusDto() {}

    public UserStatusDto(String userId, boolean isOnline) {
        this.userId = userId;
        this.isOnline = isOnline;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}

