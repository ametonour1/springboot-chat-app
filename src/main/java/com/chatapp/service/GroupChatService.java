package com.chatapp.service;

import java.security.PublicKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Collections;

import java.util.stream.Collectors;
import java.time.temporal.ChronoUnit;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.chatapp.repository.GroupChatMemberRepository;
import com.chatapp.repository.GroupChatRepository;
import com.chatapp.repository.GroupKeyRepository;
import com.chatapp.repository.GroupChatMessageRepository;
import com.chatapp.repository.GroupMessageStatusRepository;
import com.chatapp.dto.CreateGroupChatRequest;
import com.chatapp.dto.CreateGroupChatRequest.GroupMemberDto;
import com.chatapp.dto.GroupChatEncryptedKeyDto;
import com.chatapp.dto.GroupChatMessageRequest;
import com.chatapp.dto.GroupMetadataDTO;
import com.chatapp.dto.GroupReadReceiptEvent;
import com.chatapp.dto.KickMemberRequest;
import com.chatapp.dto.RecentChatterDto;
import com.chatapp.dto.UserSummaryDTO;
import com.chatapp.model.GroupChat;
import com.chatapp.model.GroupChatMember;
import com.chatapp.model.GroupChatMessage;
import com.chatapp.model.GroupKeyEntity;
import com.chatapp.model.GroupMessageStatus;

import org.springframework.messaging.simp.SimpMessagingTemplate;


@Service
public class GroupChatService {

     private final GroupChatRepository groupChatRepository;
    private final GroupChatMemberRepository groupChatMemberRepository;
    private final GroupKeyRepository groupKeyRepository;
    private final RedisService redisService;
    private final EncryptionKeyService encryptionKeyService;
    private final GroupChatMessageRepository groupChatMessageRepository;
    private final RecentChatterService recentChatterService;
    private final SimpMessagingTemplate messagingTemplate;

    private final GroupMessageStatusRepository groupMessageStatusRepository;


        @Autowired
    public GroupChatService(GroupChatRepository groupChatRepository,
                            GroupChatMemberRepository groupChatMemberRepository,
                            GroupKeyRepository groupKeyRepository,
                            RedisService redisService,
                            EncryptionKeyService encryptionKeyService,
                            GroupChatMessageRepository groupChatMessageRepository,
                            RecentChatterService recentChatterService,
                            SimpMessagingTemplate messagingTemplate,
                            GroupMessageStatusRepository groupMessageStatusRepository
                            
                            ) {
        this.groupChatRepository = groupChatRepository;
        this.groupChatMemberRepository = groupChatMemberRepository;
        this.groupKeyRepository = groupKeyRepository;
        this.redisService = redisService;
        this.encryptionKeyService = encryptionKeyService;
        this.groupChatMessageRepository = groupChatMessageRepository;
        this.recentChatterService = recentChatterService;
        this.messagingTemplate = messagingTemplate;
        this.groupMessageStatusRepository = groupMessageStatusRepository;

    }



    public GroupChat createGroupChat(CreateGroupChatRequest request) throws Exception {
  
       GroupChat groupChat = new GroupChat();
        groupChat.setName(request.getGroupName());
        groupChat.setCreatedBy(Long.parseLong(request.getFounderUserId()));
        GroupChat savedGroupChat = groupChatRepository.save(groupChat);

     for (CreateGroupChatRequest.GroupMemberDto memberDto : request.getMembers()) {
        // Save GroupKeyEntity
        GroupKeyEntity memberKey = new GroupKeyEntity();
        memberKey.setUserId(memberDto.getUserId());
        memberKey.setGroupChatId(savedGroupChat.getId());
        memberKey.setEncryptedKey(memberDto.getEncryptedKey());
        memberKey.setKeyVersion(1); // Assuming initial version is 1
        memberKey.setIv(memberDto.getIv());
        groupKeyRepository.save(memberKey);

        // Save GroupChatMember
        GroupChatMember member = new GroupChatMember();
        member.setGroupChatId(savedGroupChat.getId());
        member.setUserId(memberDto.getUserId());
        member.setAdmin(memberDto.getIsAdminUser());
        member.setJoinedAt(LocalDateTime.now((ZoneOffset.UTC)));
        groupChatMemberRepository.save(member);
      
        
        // Update Redis (if applicable)
        String groupChatterId = "group_" + savedGroupChat.getId().toString();
        redisService.addRecentChatter(memberDto.getUserId().toString(), groupChatterId);
        redisService.addGroupMember(savedGroupChat.getId(), memberDto.getUserId().toString());
    }

    // TODO update the recent chats upon creating to render UI
    recentChatterService.pushRecentChatUpdatesForGroup(savedGroupChat.getId());
    return savedGroupChat;
}
// @Transactional
// public void addMembersToGroupChat(Long groupChatId, List<GroupMemberDto> members) throws Exception {
    

//     for (GroupMemberDto memberDto  : members) {
//          String encryptedKeyBase64 = memberDto.getEncryptedKey();
//             Long userId = memberDto.getUserId();
//             boolean isAdmin = memberDto.isAdmin();

//             // Save GroupKeyEntity
//             GroupKeyEntity memberKey = new GroupKeyEntity();
//             memberKey.setUserId(userId);
//             memberKey.setGroupChatId(groupChatId);
//             memberKey.setEncryptedKey(encryptedKeyBase64);
//             memberKey.setKeyVersion(1); // or retrieve the current version
//             groupKeyRepository.save(memberKey);

//             // Save GroupChatMember
//             GroupChatMember member = new GroupChatMember();
//             member.setGroupChatId(groupChatId);
//             member.setUserId(userId);
//             member.setAdmin(isAdmin);
//             member.setJoinedAt(LocalDateTime.now());
//             groupChatMemberRepository.save(member);


//         String groupChatterId = "group_" + groupChatId.toString();
//         redisService.addRecentChatter(userId.toString(), groupChatterId);
//         redisService.addGroupMember(groupChatId,userId.toString());
//     }
// }

    public List<GroupChatEncryptedKeyDto> getEncryptedKeysForUserAndGroup(Long userId, Long groupChatId) {
        return groupKeyRepository.findByGroupChatIdAndUserId(groupChatId, userId)
            .map(entity -> List.of(
                new GroupChatEncryptedKeyDto(
                    entity.getUserId(),
                    entity.getEncryptedKey(),
                    entity.getKeyVersion(),
                    entity.getIv()
                )
            ))
            .orElse(List.of());
    }

    public Optional<GroupChat> getGroupChatById(Long id) {
        return groupChatRepository.findById(id);
    }

    public List<GroupChat> getGroupsByIds(List<String> groupIds) {
        // Convert String IDs to Long
        List<Long> ids = groupIds.stream()
                .map(Long::parseLong)
                .toList();
        return groupChatRepository.findAllById(ids);
    }


        public void sendGroupChatMessage(GroupChatMessageRequest message) {
        // For now, just print the message details
        System.out.println("Sending group chat message:");
        System.out.println("Group ID: " + message.getGroupChatId());
        System.out.println("Sender: " + message.getSenderId());
        System.out.println("Message: " + message.getContent());

        // saveMessage(message);
        // recentChatterService.pushRecentChatUpdatesForGroup(message.getGroupChatId());
        // Later we will call Kafka producer or other services here
        GroupChatMessage savedEntity = saveMessage(message);

        if (savedEntity != null) {
         
            try {
                redisService.addToGroupCache(
                    savedEntity.getGroupChatId().toString(), 
                    savedEntity
                );
            } catch (Exception e) {
                // We log but don't crash; if Redis is down, the app should still work
                e.printStackTrace();
            }
            String destination = "/topic/group/" + message.getGroupChatId();
            messagingTemplate.convertAndSend(destination, savedEntity);

         
            recentChatterService.pushRecentChatUpdatesForGroup(message.getGroupChatId());
            
            // 4. (Optional) Push to Redis for the history/cache logic we discussed
            // redisTemplate.opsForList().leftPush("group_history:" + message.getGroupChatId(), savedEntity);
        }
    }

        public GroupChatMessage saveMessage(GroupChatMessageRequest message) {
         try {
        GroupChatMessage entity = new GroupChatMessage();
        entity.setGroupChatId(message.getGroupChatId());
        entity.setSenderId(message.getSenderId());
        entity.setContent(message.getContent());
        entity.setIv(message.getIv());
        entity.setKeyVersion(message.getKeyVersion());
        entity.setTimestamp(Instant.now());
        return groupChatMessageRepository.save(entity);
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
    }
    

   public List<GroupChatMessage> getGroupMessages(String groupId, int offset, int limit) {

    // 1. Fetch from Redis using your offset
    List<GroupChatMessage> cachedMessages = redisService.getCachedMessagesWithOffset(groupId, offset, limit);
    int redisSize = (cachedMessages != null) ? cachedMessages.size() : 0;

    // 🟢 SCENARIO 1: Full Cache Hit
    // If Redis gave us the full requested limit, just return it immediately!
    if (redisSize >= limit) {
        return cachedMessages;
    }

    System.out.println("--- CACHE MISS / PARTIAL HIT ---");
    System.out.println("Redis only had " + redisSize + " messages. Fetching from Postgres...");
    
    Long parsedGroupId = Long.parseLong(groupId);

    // 🔴 SCENARIO 2: Total Cache Miss

    if (redisSize == 0) {  
        return groupChatMessageRepository.findInitialMessages(parsedGroupId, limit);
    }

    // 🟡 SCENARIO 3: Partial Cache Hit

    int remainingNeeded = limit - redisSize;

    // To prevent overlaps, grab the OLDEST message in the Redis chunk (the last index)
    GroupChatMessage oldestCachedMsg = cachedMessages.get(redisSize - 1);
    String cutoffTimestampStr = oldestCachedMsg.getTimestamp().toString();

    System.out.println("Sliding cutoff! Pulling " + remainingNeeded + " Postgres messages before: " + cutoffTimestampStr);

  
    List<GroupChatMessage> dbMessages = groupChatMessageRepository.findHistoricalGaps(
            parsedGroupId, 
            cutoffTimestampStr, 
            remainingNeeded
    );

 
    java.util.Collections.reverse(dbMessages); 

    List<GroupChatMessage> combined = new ArrayList<>(dbMessages);
    combined.addAll(cachedMessages);

    return combined;
}

    public List<GroupChatMessage> getMessagesAfter(String groupId, String lastTimestampStr) {
    
    double minScore;
    LocalDateTime postgresAfterTime;
    try {
        
        Instant instant = Instant.parse(lastTimestampStr); 
        minScore = (double) instant.toEpochMilli();
        postgresAfterTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    } catch (Exception e) {
      
        System.err.println("Failed to parse timestamp, using fallback: " + e.getMessage());
        minScore = (double) System.currentTimeMillis() - (24 * 60 * 60 * 1000);
        postgresAfterTime = LocalDateTime.now().minusDays(1);
    }

    //limit set to 10 for testing
    List<GroupChatMessage> cachedUpdates = redisService.getMessagesAfterScore(groupId, minScore, 10);
    int SYNC_LIMIT = 10;

    // If Redis is completely empty (because the cache expired after 5 days), 
    // ONLY then do we go to Postgres to get the latest messages!
    if (cachedUpdates.isEmpty()) {
        System.out.println("⚠️ Redis cache was empty (possibly expired). Fetching latest messages from Postgres...");

        Long parsedGroupId = Long.parseLong(groupId);

        // Just fetch the latest 10 from Postgres directly! No complex combining needed.
        return groupChatMessageRepository.findLatestGaps(
                parsedGroupId, 
                postgresAfterTime, 
                SYNC_LIMIT
        );
    }
    
    System.out.println("Sync Result: Found " + cachedUpdates.size() + " new messages in Redis.");
    
    return cachedUpdates;
}

public List<GroupChatMessage> getMessagesBefore(String groupId, String beforeTimestampStr, int limit) {
       
        double maxScore;
        LocalDateTime postgresBeforeTime;
        try {
            Instant instant = Instant.parse(beforeTimestampStr);
            maxScore = (double) instant.toEpochMilli();
            postgresBeforeTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        } catch (Exception e) {
            System.err.println("Failed to parse historical timestamp: " + e.getMessage());
            // Fallback: If parsing fails, use current time
            maxScore = (double) System.currentTimeMillis();
            postgresBeforeTime = LocalDateTime.now(ZoneOffset.UTC);

        }

    
        List<GroupChatMessage> olderMessages = redisService.getMessagesBeforeScore(groupId, maxScore, limit);

        System.out.println("Historical Sync: Found " + olderMessages.size() + " messages in Redis.");

        if (olderMessages.size() < limit) {
        int remainingNeeded = limit - olderMessages.size();
        System.out.println("⚠️ Redis only had " + olderMessages.size() + " messages. Fetching " + remainingNeeded + " more from Postgres...");


        Long parsedGroupId = Long.parseLong(groupId);
        // 1. DEFAULT: Use the original frontend timestamp
        String targetTimeStr = postgresBeforeTime.toString(); 
        
        // 2. THE FIX: If Redis actually gave us some messages, find the OLDEST one!
        if (olderMessages.size() > 0) {
     
            GroupChatMessage oldestRedisMsg = olderMessages.get(olderMessages.size() - 1);
            
           
            targetTimeStr = oldestRedisMsg.getTimestamp().toString();
            System.out.println("Sliding cutoff! Now looking for Postgres messages before: " + targetTimeStr);
        }

        List<GroupChatMessage> dbMessages = groupChatMessageRepository.findHistoricalGaps(
                parsedGroupId, 
                targetTimeStr, 
                remainingNeeded
        );

        System.out.println("time" + targetTimeStr);
        System.out.println("groupId" + parsedGroupId);
        System.out.println("remainingNeeded" + remainingNeeded);

        System.out.println("dbMessages" + dbMessages.size() + " messages in postgres");

        java.util.Collections.reverse(dbMessages); 

        List<GroupChatMessage> combined = new ArrayList<>(dbMessages);
        combined.addAll(olderMessages);

        return combined;
    }
     

        return olderMessages;
    }


    public void handleGroupReadReceiptOrchestration(GroupReadReceiptEvent event) {
    
    System.out.println("🧠 Orchestrating read receipt for Group " + event.getGroupChatId());

    updateRedisReadCursor(event);


    updatePostgresReadCursor(event);

    // 3. Task C: (Next Step) Trigger live WebSocket broadcast to other users
    broadcastReadReceiptToGroup(event);
}


private void updateRedisReadCursor(GroupReadReceiptEvent event) {
    String redisKey = "group:" + event.getGroupChatId() + ":read_status";
    String field = String.valueOf(event.getUserId());
    String value = String.valueOf(event.getLastReadMessageId());
    
    redisService.updateHashField(redisKey, field, value);
    System.out.println("⚡ Redis cursor updated.");
}


private void updatePostgresReadCursor(GroupReadReceiptEvent event) {
    groupMessageStatusRepository.upsertUserReadStatus(
        event.getGroupChatId(), 
        event.getUserId(), 
        event.getLastReadMessageId()
    );
    System.out.println("💾 Postgres database updated.");
}


private void broadcastReadReceiptToGroup(GroupReadReceiptEvent event) {
    String topic = "/topic/group/" + event.getGroupChatId() + "/read-status";
    
    try {
        messagingTemplate.convertAndSend(topic, event);
        System.out.println("📡 Broadcasted read receipt to live WebSocket topic: " + topic);
    } catch (Exception e) {
        System.err.println("❌ Failed to broadcast read receipt over WebSocket: " + e.getMessage());
    }
}


private Map<Long, Long> getReadCursorsFromRedis(Long groupId) {
    String redisKey = "group:" + groupId + ":read_status";
    

    Map<String, String> rawHash = redisService.getEntireHash(redisKey);
    
    if (rawHash == null || rawHash.isEmpty()) {
        return Collections.emptyMap();
    }
    
   
    return rawHash.entrySet().stream()
            .collect(Collectors.toMap(
                e -> Long.valueOf(e.getKey()),
                e -> Long.valueOf(e.getValue())
            ));
}


private Map<Long, Long> getReadCursorsFromPostgres(Long groupId) {

    List<GroupMessageStatus> statuses = groupMessageStatusRepository.findByGroupChatId(groupId);
    
    if (statuses == null || statuses.isEmpty()) {
        return Collections.emptyMap();
    }
    

    return statuses.stream()
            .collect(Collectors.toMap(
                GroupMessageStatus::getUserId,
                GroupMessageStatus::getLastReadMessageId
            ));
}


public Map<Long, Long> getReadCursors(Long groupId) {
    Map<Long, Long> cursors = getReadCursorsFromRedis(groupId);
    
    if (!cursors.isEmpty()) {
        System.out.println("⚡ Read cursors fetched from Redis for group: " + groupId);
        return cursors;
    }
    
 
    System.out.println("💾 Redis miss. Fetching read cursors from Postgres for group: " + groupId);
    cursors = getReadCursorsFromPostgres(groupId);
    
  
    if (!cursors.isEmpty()) {
        String redisKey = "group:" + groupId + ":read_status";
        cursors.forEach((userId, lastReadMessageId) -> {
            redisService.updateHashField(redisKey, String.valueOf(userId), String.valueOf(lastReadMessageId));
        });
        System.out.println("🔄 Repopulated Redis with Postgres cursors for group: " + groupId);
    }
    
    return cursors;
}

public List<UserSummaryDTO> getGroupMembers(Long groupId) {
    List<UserSummaryDTO> members = groupChatMemberRepository.findMembersByGroupId(groupId);

   
    members.forEach(member -> {
        String publicKey = redisService.getUserPublicKey(member.getUserId().toString());
        
        // If Redis is empty (evicted), you may want a fallback to the User entity 
        // or simply ensure your frontend handles a null (though Redis is usually reliable here).
        member.setPublicKey(publicKey);
    });

    return members;
}

public GroupMetadataDTO getGroupMetadata(Long groupId) {
    Integer version = groupChatRepository.findKeyVersionById(groupId);
    if (version == null) version = 1; 

    List<UserSummaryDTO> members = getGroupMembers(groupId);

    Map<Long, Long> cursors = getReadCursors(groupId);

    return GroupMetadataDTO.builder()
            .groupId(groupId)
            .currentKeyVersion(version)
            .members(members)
            .readCursors(cursors)
            .build();
}

    public boolean isUserAdmin(Long groupId, Long userId) {
            //return groupChatMemberRepository.existsByGroupChatIdAndUserIdAndIsAdminTrue(groupId, userId);
            System.out.println("Checking admin status: User=" + userId + ", Group=" + groupId);
    
           
            boolean isAdmin = groupChatMemberRepository.existsByGroupChatIdAndUserIdAndIsAdminTrue(groupId, userId);
            
            System.out.println("Result from Database: " + isAdmin);
            return isAdmin;
        }


    @Transactional
    public void kickMember(Long groupId, Long kickedUserId) {
        groupChatMemberRepository.deleteByGroupIdAndUserId(groupId, kickedUserId);
        
        redisService.removeGroupMember(groupId, kickedUserId);
        
        System.out.println("User " + kickedUserId + " removed from Group " + groupId + " in DB and Redis.");
    }

    // 4. Get current version helper
    public Integer findKeyVersionById(Long groupId) {
        return groupChatRepository.findKeyVersionById(groupId);
    }

    // 5. Increment version helper
    @Transactional
    public void incrementGroupVersion(Long groupId) {
        groupChatRepository.incrementKeyVersion(groupId);
    }

    @Transactional
    public void kickMemberAndRotate(Long groupId, Long adminId, KickMemberRequest request) {
    // 1. Authorization: Is the requester an admin?
    if (!isUserAdmin(groupId, adminId)) {
        System.out.println("User " + adminId + " is not admin for" + groupId );
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can kick members");
    }


    kickMember(groupId, request.getKickedUserId());


    incrementGroupVersion(groupId);
    int newVersion = findKeyVersionById(groupId);

    for (KickMemberRequest.GroupMemberKeyDto memberDto : request.getMembers()) { // Changed type here
        GroupKeyEntity memberKey = new GroupKeyEntity();
        memberKey.setUserId(memberDto.getUserId());
        memberKey.setGroupChatId(groupId);
        memberKey.setEncryptedKey(memberDto.getEncryptedKey());
        memberKey.setKeyVersion(newVersion);
        memberKey.setIv(memberDto.getIv());
        
        groupKeyRepository.save(memberKey);
    }
    
    
    System.out.println("Group " + groupId + " rotated to V" + newVersion + " after kick.");
}
}
