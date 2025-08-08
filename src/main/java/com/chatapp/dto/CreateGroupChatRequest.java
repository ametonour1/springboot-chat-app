package com.chatapp.dto;

import java.util.List;

public class CreateGroupChatRequest {
    private String groupName;
    private String founderUserId;
      private List<Long> memberIds; 

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

     public List<Long> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<Long> memberIds) {
        this.memberIds = memberIds;
    }
}
