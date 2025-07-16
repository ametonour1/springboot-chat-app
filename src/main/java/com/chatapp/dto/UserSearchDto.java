package com.chatapp.dto;

public class UserSearchDto {
    private Long id;
    private String username;

    public UserSearchDto(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
}