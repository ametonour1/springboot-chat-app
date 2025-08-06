package com.chatapp.dto;

public class UserSearchDto {
    private Long userId;
    private String username;
       private String publicKey;

    public UserSearchDto(Long userId, String username,String publicKey) {
        this.userId = userId;
        this.username = username;
        this.publicKey = publicKey;
    }

    public Long getuserId() { return userId; }
    public String getUsername() { return username; }
     public String getPublicKey() { return publicKey; }
}