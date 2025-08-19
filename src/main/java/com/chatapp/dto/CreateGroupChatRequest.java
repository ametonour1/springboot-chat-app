package com.chatapp.dto;

import java.util.List;

public class CreateGroupChatRequest {
    private String groupName;
    private String founderUserId;
    private List<GroupMemberDto> members;

    public CreateGroupChatRequest() {
    }

    // Getters and Setters
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getFounderUserId() {
        return founderUserId;
    }

    public void setFounderUserId(String founderUserId) {
        this.founderUserId = founderUserId;
    }

    public List<GroupMemberDto> getMembers() {
        return members;
    }

    public void setMembers(List<GroupMemberDto> members) {
        this.members = members;
    }



    // New inner class for group members
   public static class GroupMemberDto {
    private Long userId;
    private String encryptedKey;
    private boolean isAdminUser; // Renamed from isAdmin
    private String iv; 

    public GroupMemberDto() {
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEncryptedKey() {
        return encryptedKey;
    }

    public void setEncryptedKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

    // Updated getter for the new field name
    public boolean getIsAdminUser() {
        return isAdminUser;
    }

    // Updated setter for the new field name
    public void setIsAdminUser(boolean isAdminUser) {
        this.isAdminUser = isAdminUser;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }
}
}