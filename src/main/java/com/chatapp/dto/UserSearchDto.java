package com.chatapp.dto;

public class UserSearchDto {
    private Long userId;
    private String username;

    public UserSearchDto(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public Long getuserId() { return userId; }
    public String getUsername() { return username; }
}