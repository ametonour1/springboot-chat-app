package com.chatapp.dto;

public class RecentChatterDto {
    private String userId;
    private String username;
    private String profileImageUrl; // optional
    private boolean isOnline;
    private boolean hasUnreadMessage;
    private String publicKey;
    private String type;

    public RecentChatterDto() {
    }

    public RecentChatterDto(String userId, String username, boolean isOnline, boolean hasUnreadMessage, String publicKey,String type) {
        this.userId = userId;
        this.username = username;
        this.isOnline = isOnline;
        this.hasUnreadMessage = hasUnreadMessage;
        this.publicKey = publicKey;
        this.type = type;
       
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

        public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    
    public boolean isHasUnreadMessage() {
        return hasUnreadMessage;
    }

    public void setHasUnreadMessage(boolean hasUnreadMessage) {
        this.hasUnreadMessage = hasUnreadMessage;
    }

     public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
