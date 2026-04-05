package com.chatapp.dto;

public class UserSearchDto {
    private Long userId;
    private String username;
       private String publicKey;
       private String type;

    public UserSearchDto(Long userId, String username,String publicKey , String type) {
        this.userId = userId;
        this.username = username;
        this.publicKey = publicKey;
        this.type = type;
    }

    public Long getuserId() { return userId; }
    public String getUsername() { return username; }
     public String getPublicKey() { return publicKey; }
    
    public String getType(){ return type;}
    public void setType(String type) {
        this.type = type;
    }
}