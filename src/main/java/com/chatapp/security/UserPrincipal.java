package com.chatapp.security;

public class UserPrincipal {
    private final String email;
    private final Long userId;

    public UserPrincipal(String email, Long userId) {
        this.email = email;
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public Long getUserId() {
        return userId;
    }
}