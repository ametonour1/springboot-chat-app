package com.chatapp.service;

import java.security.PublicKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatapp.repository.GroupChatMemberRepository;
import com.chatapp.repository.GroupChatRepository;
import com.chatapp.repository.GroupKeyRepository;
import com.chatapp.repository.GroupChatMessageRepository;
import com.chatapp.dto.CreateGroupChatRequest;
import com.chatapp.dto.CreateGroupChatRequest.GroupMemberDto;
import com.chatapp.dto.GroupChatEncryptedKeyDto;
import com.chatapp.dto.GroupChatMessageRequest;
import com.chatapp.dto.RecentChatterDto;
import com.chatapp.model.GroupChat;
import com.chatapp.model.GroupChatMember;
import com.chatapp.model.GroupChatMessage;
import com.chatapp.model.GroupKeyEntity;
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


        @Autowired
    public GroupChatService(GroupChatRepository groupChatRepository,
                            GroupChatMemberRepository groupChatMemberRepository,
                            GroupKeyRepository groupKeyRepository,
                            RedisService redisService,
                            EncryptionKeyService encryptionKeyService,
                            GroupChatMessageRepository groupChatMessageRepository,
                            RecentChatterService recentChatterService,
                            SimpMessagingTemplate messagingTemplate
                            
                            ) {
        this.groupChatRepository = groupChatRepository;
        this.groupChatMemberRepository = groupChatMemberRepository;
        this.groupKeyRepository = groupKeyRepository;
        this.redisService = redisService;
        this.encryptionKeyService = encryptionKeyService;
        this.groupChatMessageRepository = groupChatMessageRepository;
        this.recentChatterService = recentChatterService;
        this.messagingTemplate = messagingTemplate;

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
        member.setJoinedAt(LocalDateTime.now());
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
        entity.setTimestamp(LocalDateTime.now());
        return groupChatMessageRepository.save(entity);
    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }
    }
    

    public List<GroupChatMessage> getGroupMessages(String groupId, int offset, int limit) {

        List<GroupChatMessage> cachedMessages = redisService.getCachedMessagesWithOffset(groupId, offset, limit);

  
        if (cachedMessages.size() < limit) {
           // This will show up in your IDE console in white text
            System.out.println("--- CACHE MISS ---");
            System.out.println("Group ID: " + groupId);
            System.out.println("Offset: " + offset);
            System.out.println("Redis returned: " + cachedMessages.size() + " messages.");
            System.out.println("Action: Need to fetch from Postgres next.");
            System.out.println("------------------");
            // TODO: Layer 4 - Fetch the remaining 'n' messages from Postgres
            // List<GroupChatMessage> dbMessages = groupMessageRepository.findWithOffset(...);
            // Combine them...
        }

        return cachedMessages;
    }

    public List<GroupChatMessage> getMessagesAfter(String groupId, String lastTimestampStr) {
    // 1. Convert the ISO String from frontend into Epoch Milliseconds (Double for Redis)
    double minScore;
    try {
        // This handles "2026-03-29T11:37:21.56819"
        Instant instant = Instant.parse(lastTimestampStr); // Appending Z for UTC if not present
        minScore = (double) instant.toEpochMilli();
    } catch (Exception e) {
        // Fallback: If parsing fails, use current time minus 24 hours so the user gets something!
        System.err.println("Failed to parse timestamp, using fallback: " + e.getMessage());
        minScore = (double) System.currentTimeMillis() - (24 * 60 * 60 * 1000);
    }

    // 2. Fetch the updates from Redis
    List<GroupChatMessage> cachedUpdates = redisService.getMessagesAfterScore(groupId, minScore);

    // 3. YOUR NOTE: The Next Step!
    // If the user's last message is older than what Redis holds, 
    // Redis might return nothing or only partial data.
    // That's when we will fall back to Postgres. For now, let's look at the Redis result.
    
    System.out.println("Sync Result: Found " + cachedUpdates.size() + " new messages in Redis.");
    
    return cachedUpdates;
}
}
