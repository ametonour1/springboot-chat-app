package com.chatapp.dto;
import java.util.List;
import lombok.Data;

@Data
public class KickMemberRequest {
    private Long kickedUserId;
    private List<GroupMemberKeyDto> members; 

    // Move this INSIDE the KickMemberRequest braces
    @Data
    public static class GroupMemberKeyDto { 
        private Long userId;
        private String encryptedKey;
        private String iv;
    }
}