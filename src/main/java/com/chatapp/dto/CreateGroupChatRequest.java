package com.chatapp.dto;

public class CreateGroupChatRequest {
    private String groupName;
    private String founderUserId;

    public CreateGroupChatRequest() {
    }

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
}
