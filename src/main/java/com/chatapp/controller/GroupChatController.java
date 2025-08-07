package com.chatapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.chatapp.dto.CreateGroupChatRequest;
import com.chatapp.dto.GroupChatEncryptedKeyDto;
import com.chatapp.model.GroupChat;
import com.chatapp.security.UserPrincipal;
import com.chatapp.service.GroupChatService;

@RestController
@RequestMapping("/group-chats")
public class GroupChatController {

    private final GroupChatService groupChatService;

    @Autowired
    public GroupChatController(GroupChatService groupChatService) {
        this.groupChatService = groupChatService;
    }

    @PostMapping("create")
    public ResponseEntity<GroupChat> createGroupChat(@RequestBody CreateGroupChatRequest request) {
        try {
            GroupChat groupChat = groupChatService.createGroupChat(request.getGroupName(), request.getFounderUserId());
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
}   
