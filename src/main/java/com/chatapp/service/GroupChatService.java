package com.chatapp.service;

import java.security.PublicKey;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chatapp.repository.GroupChatMemberRepository;
import com.chatapp.repository.GroupChatRepository;
import com.chatapp.repository.GroupKeyRepository;
import com.chatapp.dto.GroupChatEncryptedKeyDto;
import com.chatapp.model.GroupChat;
import com.chatapp.model.GroupChatMember;
import com.chatapp.model.GroupKeyEntity;
@Service
public class GroupChatService {

     private final GroupChatRepository groupChatRepository;
    private final GroupChatMemberRepository groupChatMemberRepository;
    private final GroupKeyRepository groupKeyRepository;
    private final RedisService redisService;
    private final EncryptionKeyService encryptionKeyService;

        @Autowired
    public GroupChatService(GroupChatRepository groupChatRepository,
                            GroupChatMemberRepository groupChatMemberRepository,
                            GroupKeyRepository groupKeyRepository,
                            RedisService redisService,
                            EncryptionKeyService encryptionKeyService) {
        this.groupChatRepository = groupChatRepository;
        this.groupChatMemberRepository = groupChatMemberRepository;
        this.groupKeyRepository = groupKeyRepository;
        this.redisService = redisService;
        this.encryptionKeyService = encryptionKeyService;
    }



    public GroupChat createGroupChat(String groupName, String founderUserId , List<Long> memberIds) throws Exception {
    // 1. Generate a new AES group key
    String founderPublicKeyStr = redisService.getUserPublicKey(founderUserId);
    SecretKey aesKey = encryptionKeyService.generateAESKey();

    // 2. Load founder's public key from stored string
    PublicKey founderPubKey = encryptionKeyService.loadPublicKeyFromString(founderPublicKeyStr);

    // 3. Encrypt the AES key with founder's public key
    byte[] encryptedKey = encryptionKeyService.encryptWithPublicKey(aesKey.getEncoded(), founderPubKey);
    String encryptedKeyBase64 = Base64.getEncoder().encodeToString(encryptedKey);

    // 4. Create GroupChat entity and set encrypted group key
    GroupChat groupChat = new GroupChat();
    groupChat.setName(groupName);
    groupChat.setCreatedBy(Long.parseLong(founderUserId));

    // 5. Save group chat in DB
    GroupChat savedGroupChat = groupChatRepository.save(groupChat);

    // 6. Add founder as admin member with encrypted AES key
    GroupKeyEntity memberKey = new GroupKeyEntity();
  
    memberKey.setUserId(Long.parseLong(founderUserId));
    memberKey.setEncryptedKey(encryptedKeyBase64);
    memberKey.setKeyVersion(1);  // Start at version 1
    memberKey.setGroupChatId(savedGroupChat.getId());
    groupKeyRepository.save(memberKey);

    // 7. Add founder to members table as admin
    GroupChatMember member = new GroupChatMember();
    member.setGroupChatId(savedGroupChat.getId());
    member.setUserId(Long.parseLong(founderUserId));
    member.setAdmin(true);
    member.setJoinedAt(LocalDateTime.now());
    groupChatMemberRepository.save(member);

    addMembersToGroupChat(savedGroupChat.getId(), memberIds, aesKey);
    return savedGroupChat;
}

public void addMembersToGroupChat(Long groupChatId, List<Long> memberIds,SecretKey aesKey) throws Exception {
    

    for (Long userId : memberIds) {
        String publicKeyStr = redisService.getUserPublicKey(userId.toString());
        PublicKey pubKey = encryptionKeyService.loadPublicKeyFromString(publicKeyStr);
        byte[] encryptedKey = encryptionKeyService.encryptWithPublicKey(aesKey.getEncoded(), pubKey);
        String encryptedKeyBase64 = Base64.getEncoder().encodeToString(encryptedKey);

        // Save GroupKeyEntity
        GroupKeyEntity memberKey = new GroupKeyEntity();
        memberKey.setUserId(userId);
        memberKey.setGroupChatId(groupChatId);
        memberKey.setEncryptedKey(encryptedKeyBase64);
        memberKey.setKeyVersion(1);
        groupKeyRepository.save(memberKey);

        // Save GroupChatMember
        GroupChatMember member = new GroupChatMember();
        member.setGroupChatId(groupChatId);
        member.setUserId(userId);
        member.setAdmin(false); // normal member
        member.setJoinedAt(LocalDateTime.now());
        groupChatMemberRepository.save(member);
    }
}

    public List<GroupChatEncryptedKeyDto> getEncryptedKeysForUserAndGroup(Long userId, Long groupChatId) {
        return groupKeyRepository.findByGroupChatIdAndUserId(groupChatId, userId)
            .map(entity -> List.of(
                new GroupChatEncryptedKeyDto(
                    entity.getUserId(),
                    entity.getEncryptedKey(),
                    entity.getKeyVersion()
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

    
}
