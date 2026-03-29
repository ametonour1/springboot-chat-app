package com.chatapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.chatapp.dto.CreateGroupChatRequest;
import com.chatapp.dto.GroupChatEncryptedKeyDto;
import com.chatapp.model.GroupChat;
import com.chatapp.model.GroupChatMessage;
import com.chatapp.security.UserPrincipal;
import com.chatapp.service.GroupChatService;

@RestController
@RequestMapping("/api/group-chats")
public class GroupChatController {

    private final GroupChatService groupChatService;

    @Autowired
    public GroupChatController(GroupChatService groupChatService) {
        this.groupChatService = groupChatService;
    }

    @PostMapping("create")
    public ResponseEntity<GroupChat> createGroupChat(@RequestBody CreateGroupChatRequest request) {
        try {
            GroupChat groupChat = groupChatService.createGroupChat(request);
            System.out.println("groupRequest " + request);
               if (request.getMembers() != null) {
                for (CreateGroupChatRequest.GroupMemberDto memberDto : request.getMembers()) {
                   System.out.println("User ID: {}, isAdmin: {}"+ memberDto.getUserId()+ memberDto.getIsAdminUser());
                }
        }
            return ResponseEntity.ok(groupChat);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/{id}/keys")
    public ResponseEntity<List<GroupChatEncryptedKeyDto>> getEncryptedKeys(@PathVariable Long id, Authentication authentication) {
         UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        Long userId = principal.getUserId();
        
        List<GroupChatEncryptedKeyDto> keys = groupChatService.getEncryptedKeysForUserAndGroup(userId, id);

            if (keys.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(keys);
}

@GetMapping("/{groupId}/messages")
    public ResponseEntity<List<GroupChatMessage>> getGroupMessages(
            @PathVariable String groupId,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "20") int limit) {
        
        System.out.println("API Request: Fetching group " + groupId + " | Offset: " + offset);
        
        List<GroupChatMessage> messages = groupChatService.getGroupMessages(groupId, offset, limit);
        
        return ResponseEntity.ok(messages);
    }

 @GetMapping("/{groupId}/sync")
public ResponseEntity<List<GroupChatMessage>> syncGroupMessages(
        @PathVariable String groupId,
        @RequestParam String lastTimestamp) {
    
    System.out.println("API Request: Syncing group " + groupId + " since " + lastTimestamp);
    
    List<GroupChatMessage> messages = groupChatService.getMessagesAfter(groupId, lastTimestamp);
    
    return ResponseEntity.ok(messages);
}
}   
