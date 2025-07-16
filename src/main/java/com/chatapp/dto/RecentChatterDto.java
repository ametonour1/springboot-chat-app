package com.chatapp.dto;

public class RecentChatterDto {
    private String userId;
    private String username;
    private String profileImageUrl; // optional

    public RecentChatterDto() {
    }

    public RecentChatterDto(String userId, String username) {
        this.userId = userId;
        this.username = username;
       
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
