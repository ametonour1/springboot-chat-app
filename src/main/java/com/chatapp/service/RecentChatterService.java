package com.chatapp.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.chatapp.dto.RecentChatterDto;
import com.chatapp.model.GroupChat;
import com.chatapp.model.MessageStatus;
import com.chatapp.model.User;
import com.chatapp.repository.ChatMessageRepository;
import com.chatapp.repository.GroupChatRepository;


@Service
public class RecentChatterService {
    private final RedisService redisService;
    private final UserService userService;
   
    private final SimpMessagingTemplate messagingTemplate;
     private final ChatMessageRepository chatMessageRepository;
     private final GroupChatRepository groupChatRepository;

    public RecentChatterService(RedisService redisService,
                                UserService userService,
                                 SimpMessagingTemplate messagingTemplate,
                                 ChatMessageRepository chatMessageRepository,
                                 GroupChatRepository groupChatRepository
                                 ) {
        this.redisService = redisService;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
        this.chatMessageRepository = chatMessageRepository;
        this.groupChatRepository = groupChatRepository;
    }

 public List<RecentChatterDto> getRecentChattersWithDetails(String userId) {
    List<String> chatterIds = redisService.getRecentChatters(userId);

    // Separate chatter IDs into userIds and groupIds
    List<String> userIds = new ArrayList<>();
    List<String> groupIds = new ArrayList<>();

    for (String id : chatterIds) {
        if (id.startsWith("group_")) {
            groupIds.add(id.substring(6)); // 6 = length of "group_"
        } else if (id.startsWith("user_")) {
            userIds.add(id.substring(5)); // 5 = length of "user_"
        } else {
            userIds.add(id);
        }
    }

    // Fetch DB data
    Map<String, User> userMap = userService.getUsersByIds(userIds).stream()
        .collect(Collectors.toMap(u -> u.getId().toString(), u -> u));

    Map<String, GroupChat> groupMap = getGroupsByIds(groupIds).stream()
        .collect(Collectors.toMap(g -> g.getId().toString(), g -> g)); // no prefix in key

    // Batch Redis lookups
    Map<String, Boolean> onlineMap = redisService.getUsersOnlineStatus(userIds);
    Map<String, String> publicKeyMap = redisService.getUsersPublicKeys(userIds);
    Map<String, Boolean> unreadMap = getUsersUnreadStatus(userIds, userId);

    // Build final list preserving Redis order
    return chatterIds.stream()
        .map(id -> {
            if (id.startsWith("group_")) {
                String gid = id.substring(6);
                GroupChat group = groupMap.get(gid);
                if (group != null) {
                    return new RecentChatterDto(
                        gid,
                        group.getName(),
                        false,
                       false,
                        null,
                        "GROUP"
                    );
                }
            } else if (id.startsWith("user_")) {
                String uid = id.substring(5);
                User user = userMap.get(uid);
                if (user != null) {
                    return new RecentChatterDto(
                        uid,
                        user.getUsername(),
                        onlineMap.getOrDefault(uid, false),
                         unreadMap.getOrDefault(uid, false), 
                        publicKeyMap.get(uid),
                        "USER"
                    );
                }
            }
            return null;
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
}

    public void updateRecentChats(String userId, String chatterId) {
        
            String prefixedChatterId = "user_" + chatterId;
            redisService.addRecentChatter(userId, prefixedChatterId);


            String prefixedUserId = "user_" + userId;
            redisService.addRecentChatter(chatterId, prefixedUserId);



    }

    public void updateRecentGroupChat(String userId, String groupId) {
    String prefixedGroupId = "group_" + groupId;
    redisService.addRecentChatter(userId, prefixedGroupId);
}


    public void emitRecentChatsUpdate(String userId, List<RecentChatterDto> chats) {
        System.out.println("emitChat" + userId + chats);

         messagingTemplate.convertAndSend("/topic/recent-chats/" + userId, chats);
    
    }

    public Map<String, Boolean> getUsersUnreadStatus(List<String> userIds, String recipientId) {
    if (userIds.isEmpty()) {
        return Collections.emptyMap();
    }

    List<Long> senderIds = userIds.stream()
                                  .map(Long::valueOf)
                                  .collect(Collectors.toList());
    Long recipient = Long.valueOf(recipientId);
    List<MessageStatus> unreadStatuses = List.of(MessageStatus.SENT, MessageStatus.DELIVERED);

    List<ChatMessageRepository.SenderUnreadStatus> unreadStatusesList =
        chatMessageRepository.findUnreadStatusBySenderIdsAndRecipientId(senderIds, recipient, unreadStatuses);

    Map<String, Boolean> unreadMap = new HashMap<>();
    // Initialize all to false (no unread)
    userIds.forEach(id -> unreadMap.put(id, false));

    // Mark those senders who have unread messages
    for (ChatMessageRepository.SenderUnreadStatus status : unreadStatusesList) {
        unreadMap.put(status.getSenderId().toString(), status.getHasUnread());
    }

    return unreadMap;
}

    public void pushRecentChatUpdatesForGroup(Long groupId) {
    // Get group members from Redis
    Set<String> members = redisService.getGroupMembers(groupId);
    if (members == null || members.isEmpty()) return;

    // Iterate over members
    for (String memberId : members) {
        // Update Redis recent chat data for each member
       updateRecentGroupChat(memberId, groupId.toString()); // isGroup = true

        // Push updates if member is online
        if (redisService.isUserOnline(memberId)) {
            List<RecentChatterDto> chats = getRecentChattersWithDetails(memberId);
            emitRecentChatsUpdate(memberId, chats);
        }
    }
}

  public List<GroupChat> getGroupsByIds(List<String> groupIds) {
        // Convert String IDs to Long
        List<Long> ids = groupIds.stream()
                .map(Long::parseLong)
                .toList();
        return groupChatRepository.findAllById(ids);
    }
}