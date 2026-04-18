package com.chatapp.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


import com.chatapp.model.ChatMessageEntity;
import com.chatapp.model.GroupChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.time.ZoneOffset;

@Service
public class RedisService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String ONLINE_STATUS_KEY = "user:online:";
    private static final String USER_SOCKET_KEY = "user:socket:";
    private static final String RECENT_CHATTERS_KEY_PREFIX = "recent_chatters_for_user:";
    private static final String USER_SOCKETS_KEY = "user:sockets:";
    private static final String SOCKET_USER_KEY = "socket:user:";
    private static final String USER_LAST_SEEN_KEY = "user:lastseen:";
    private static final String CHATTED_WITH_KEY_PREFIX = "recent:chatted:with:";
    private static final String USER_PUBLIC_KEY = "public_key:";
    private static final String GROUP_MEMBERS_KEY = "members:group:";



    private final ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static final int MAX_MESSAGES = 10;
    private static final int TTL_SECONDS = 86400; // 24 hours



    // Mark user as online in Redis
    public void markUserOnline(String userId) {
        redisTemplate.opsForValue().set(ONLINE_STATUS_KEY + userId, "true", 2, TimeUnit.MINUTES);  // Keep user online for 30 minutes
    }

    // Mark user as offline in Redis
    public void markUserOffline(String userId) {
        redisTemplate.delete(ONLINE_STATUS_KEY + userId);
    }

    // Check if user is online
    public boolean isUserOnline(String userId) {
       Long count = getUserConnectionCount(userId);
       return count != null && count > 0;
    }


    // Optional: Set user session expiry time in Redis
    public void setUserSessionExpiry(String userId, long minutes) {
        redisTemplate.expire(ONLINE_STATUS_KEY + userId, minutes, TimeUnit.MINUTES);
    }


    public void storeUserSocketSession(String userId, String sessionId) {
        //redisTemplate.opsForValue().set(USER_SOCKET_KEY + userId, sessionId);
        redisTemplate.opsForSet().add(USER_SOCKETS_KEY+ userId, sessionId);
        redisTemplate.opsForValue().set(SOCKET_USER_KEY + sessionId, userId);
    }

    public void removeUserSocketSession(String userId, String sessionId) {
    redisTemplate.opsForSet().remove(USER_SOCKETS_KEY + userId, sessionId);
    redisTemplate.delete(SOCKET_USER_KEY + sessionId);
    }

    public String getUserIdFromSession(String sessionId) {
        return redisTemplate.opsForValue().get(SOCKET_USER_KEY + sessionId);
    }

    public Long getUserConnectionCount(String userId) {
        return redisTemplate.opsForSet().size(USER_SOCKETS_KEY + userId);
    }

    public void removeSessionIdMapping(String sessionId) {
    redisTemplate.delete(SOCKET_USER_KEY + sessionId);
    }

    public void updateUserLastSeen(String userId) {
    String timestamp = Instant.now().toString(); // or use System.currentTimeMillis() if you prefer epoch millis
     String key = USER_LAST_SEEN_KEY + userId;
    redisTemplate.opsForValue().set(key, timestamp);
    }

    public void removeAllSessionsForUser(String userId) {
        String userSocketKey = USER_SOCKETS_KEY + userId;

        // Get all session IDs for the user
        Set<String> sessionIds = redisTemplate.opsForSet().members(userSocketKey);
        if (sessionIds != null) {
            for (String sessionId : sessionIds) {
                // Remove reverse mapping
                redisTemplate.delete(SOCKET_USER_KEY + sessionId);
            }
        }

        // Delete the user's session set key
        redisTemplate.delete(userSocketKey);
    }
    // public String getUserSocketSession(String userId) {
    //     return redisTemplate.opsForValue().get(USER_SOCKET_KEY + userId);
    // }

    // public void deleteUserSocketSession(String userId) {
    //     redisTemplate.delete(USER_SOCKET_KEY + userId);
    // }

    public void addChattedWith(String targetUserId, String chatterId) {
    String key = CHATTED_WITH_KEY_PREFIX + targetUserId;
    redisTemplate.opsForSet().add(key, chatterId);
    }
    public void removeChattedWith(String targetUserId, String chatterId) {
    String key = CHATTED_WITH_KEY_PREFIX + targetUserId;
    // Removes the chatterId from the Set
    redisTemplate.opsForSet().remove(key, chatterId);
}

    public Set<String> getUsersWhoChattedWith(String userId) {
        String key = CHATTED_WITH_KEY_PREFIX + userId;
        return redisTemplate.opsForSet().members(key);
    }

    public void addRecentChatter(String userId, String chatterId) {

    String RECENT_CHATTERS_KEY = RECENT_CHATTERS_KEY_PREFIX + userId; 

        redisTemplate.opsForList().remove(RECENT_CHATTERS_KEY, 0, chatterId);
       
        redisTemplate.opsForList().leftPush(RECENT_CHATTERS_KEY, chatterId);

        redisTemplate.opsForList().trim(RECENT_CHATTERS_KEY, 0, 10);

        addChattedWith(userId, chatterId);
    }

    public void removeRecentChatter(String userId, String chatterId) {
    String RECENT_CHATTERS_KEY = RECENT_CHATTERS_KEY_PREFIX + userId; 

    redisTemplate.opsForList().remove(RECENT_CHATTERS_KEY, 0, chatterId);
   
    removeChattedWith(userId, chatterId);
}
    public List<String> getRecentChatters(String userId) {

        String RECENT_CHATTERS_KEY = RECENT_CHATTERS_KEY_PREFIX + userId; 
        // Get all recent chatters
        return redisTemplate.opsForList().range(RECENT_CHATTERS_KEY, 0, -1);
    }

    public String getChatKey(Long userId1, Long userId2) {
    Long low = Math.min(userId1, userId2);
    Long high = Math.max(userId1, userId2);
    return "chat:messages:" + low + ":" + high;
    }


    public void addMessageToCache(Long senderId, Long recipientId, ChatMessageEntity message) {
        try {
            String key = getChatKey(senderId, recipientId);
            String jsonMessage = objectMapper.writeValueAsString(message);

            redisTemplate.opsForList().leftPush(key, jsonMessage);  // lpush
            redisTemplate.opsForList().trim(key, 0, MAX_MESSAGES - 1);  // ltrim
            redisTemplate.expire(key, Duration.ofSeconds(TTL_SECONDS));  

        } catch (Exception e) {
            // handle serialization error or redis error here
            e.printStackTrace();
        }
    }

    public List<ChatMessageEntity> getCachedMessages(Long userId1, Long userId2) {
    try {
        String key = getChatKey(userId1, userId2);
        List<String> cachedJsonMessages = redisTemplate.opsForList().range(key, 0, MAX_MESSAGES - 1);

        List<ChatMessageEntity> messages = new ArrayList<>();

        for (String json : cachedJsonMessages) {
            ChatMessageEntity message = objectMapper.readValue(json, ChatMessageEntity.class);
            messages.add(message);
        }

        // Optionally reverse list if you want oldest first instead of newest first
        Collections.reverse(messages);

        return messages;

    } catch (Exception e) {
        e.printStackTrace();
        return Collections.emptyList();
    }
}

   public void updateCachedMessageStatus(ChatMessageEntity updatedMessage) {
        try {
            String cacheKey = getChatKey(updatedMessage.getSenderId(), updatedMessage.getRecipientId());
            List<String> cachedMessages = redisTemplate.opsForList().range(cacheKey, 0, -1);
            if (cachedMessages == null) return;

            for (int i = 0; i < cachedMessages.size(); i++) {
                ChatMessageEntity cachedMsg = objectMapper.readValue(cachedMessages.get(i), ChatMessageEntity.class);
                if (cachedMsg.getId().equals(updatedMessage.getId())) {
                    String updatedJson = objectMapper.writeValueAsString(updatedMessage);
                    redisTemplate.opsForList().set(cacheKey, i, updatedJson);
                    break;
                }
            }
        } catch (Exception e) {
            // You can log the error here for debugging
            e.printStackTrace();
        }
    }
    public void cacheUserPublicKey(String userId, String publicKey) {
        redisTemplate.opsForValue().set(USER_PUBLIC_KEY + userId, publicKey);
    }

    public String getUserPublicKey(String userId) {
        return redisTemplate.opsForValue().get(USER_PUBLIC_KEY + userId);
    }

    public Map<String, String> getUsersPublicKeys(List<String> userIds) {
    if (userIds == null || userIds.isEmpty()) {
        return Collections.emptyMap();
    }

    List<String> keys = userIds.stream()
        .map(id -> USER_PUBLIC_KEY + id)
        .collect(Collectors.toList());

    List<String> values = redisTemplate.opsForValue().multiGet(keys);

    Map<String, String> result = new HashMap<>();
    for (int i = 0; i < userIds.size(); i++) {
        result.put(userIds.get(i), values.get(i));
    }
    return result;
}
    public Map<String, Boolean> getUsersOnlineStatus(List<String> userIds) {
    if (userIds == null || userIds.isEmpty()) {
        return Collections.emptyMap();
    }

    Map<String, Boolean> result = new HashMap<>();
    for (String userId : userIds) {
        Long count = redisTemplate.opsForSet().size(USER_SOCKETS_KEY + userId);
        result.put(userId, count != null && count > 0);
    }
    return result;
}

    
    public void addGroupMember(Long groupId, String userId) {
        String membersKey = GROUP_MEMBERS_KEY + groupId;
        redisTemplate.opsForSet().add(membersKey, userId);
    }


    public Set<String> getGroupMembers(Long groupId) {
        String membersKey = GROUP_MEMBERS_KEY + groupId;
        Set<String> members = redisTemplate.opsForSet().members(membersKey);
        return members != null ? members : Collections.emptySet();
    }

    public void removeGroupMember(Long groupId, long kickedUserId) {
    String membersKey = GROUP_MEMBERS_KEY + groupId;
    String userIdStr = String.valueOf(kickedUserId);
  
    redisTemplate.opsForSet().remove(membersKey, userIdStr);
}

    private static final String GROUP_CACHE_PREFIX = "group:cache:";

public void addToGroupCache(String groupId, GroupChatMessage message) {
 try {
        String key = GROUP_CACHE_PREFIX + groupId;
        
        // 1. Manually convert Object to JSON String (Matches your 1v1 style)
        String jsonMessage = objectMapper.writeValueAsString(message);

        // 2. Convert LocalDateTime to Double score
        double score = message.getTimestamp()
                              .atZone(ZoneId.systemDefault())
                              .toInstant()
                              .toEpochMilli();

        // 3. Add to Sorted Set (This works because jsonMessage is a String)
        redisTemplate.opsForZSet().add(key, jsonMessage, score);

        // 4. TTL and Trim
        redisTemplate.expire(key, Duration.ofHours(24));
        Long count = redisTemplate.opsForZSet().zCard(key);
        if (count != null && count > 100) {
            redisTemplate.opsForZSet().removeRange(key, 0, count - 101);
        }
    } catch (Exception e) {
         e.printStackTrace();
    }
}

public List<GroupChatMessage> getCachedMessagesWithOffset(String groupId, int offset, int limit) {
    String key = "group:cache:" + groupId;
    
    // Redis returns Set<String> because your template is <String, String>
    Set<String> cachedJsonMsgs = redisTemplate.opsForZSet().reverseRange(key, offset, offset + limit - 1);

    if (cachedJsonMsgs == null || cachedJsonMsgs.isEmpty()) {
        return Collections.emptyList();
    }

    List<GroupChatMessage> messages = new ArrayList<>();
    try {
        for (String json : cachedJsonMsgs) {
            // Manually deserialize just like your 1v1 getCachedMessages
            GroupChatMessage msg = objectMapper.readValue(json, GroupChatMessage.class);
            messages.add(msg);
        }
    } catch (Exception e) {
         e.printStackTrace();

    }

    Collections.reverse(messages); // Oldest to Newest for the UI
    return messages;
}

public List<GroupChatMessage> getMessagesAfterScore(String groupId, double minScore, int limit) {
    String key = "group:cache:" + groupId;
    
    // We start at minScore + 1 to exclude the message they already have
    double startScore = minScore + 1.0;
    double endScore = Double.MAX_VALUE; // All the way to the newest message

    // Fetch the raw JSON strings from the Sorted Set
    Set<String> jsonMsgs = redisTemplate.opsForZSet().reverseRangeByScore(key, startScore, endScore,0  ,limit);

    if (jsonMsgs == null || jsonMsgs.isEmpty()) {
        return Collections.emptyList();
    }

      List<GroupChatMessage> messages = new ArrayList<>();
    try {
        for (String json : jsonMsgs) {
            // Manually deserialize just like your 1v1 getCachedMessages
            GroupChatMessage msg = objectMapper.readValue(json, GroupChatMessage.class);
            messages.add(msg);
        }
    } catch (Exception e) {
         e.printStackTrace();

    }

    Collections.reverse(messages); // Oldest to Newest for the UI
    return messages;
}

public List<GroupChatMessage> getMessagesBeforeScore(String groupId, double maxScore, int limit) {
    String key = "group:cache:" + groupId;
    
    // We start looking just below the user's oldest message
    double endScore = maxScore - 1.0; 
    double startScore = 0; // Go as far back as Redis has memory for

    // Get the messages in descending order (newest of the old messages first)
    Set<String> jsonMsgs = redisTemplate.opsForZSet().reverseRangeByScore(key, startScore, endScore, 0, limit);

    if (jsonMsgs == null || jsonMsgs.isEmpty()) {
        return Collections.emptyList();
    }

       List<GroupChatMessage> messages = new ArrayList<>();
    try {
        for (String json : jsonMsgs) {
            // Manually deserialize just like your 1v1 getCachedMessages
            GroupChatMessage msg = objectMapper.readValue(json, GroupChatMessage.class);
            messages.add(msg);
        }
    } catch (Exception e) {
         e.printStackTrace();

    }

    Collections.reverse(messages); // Oldest to Newest for the UI
    return messages;
}

public void updateHashField(String key, String field, String value) {
    try {
        redisTemplate.opsForHash().put(key, field, value);
    } catch (Exception e) {
        System.err.println("❌ Failed to update Redis hash for key: " + key + ". Error: " + e.getMessage());
    }
}

public Map<String, String> getEntireHash(String key) {
        try {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            
            if (entries.isEmpty()) {
                return Collections.emptyMap();
            }
            
            return entries.entrySet().stream()
                    .collect(Collectors.toMap(
                        e -> String.valueOf(e.getKey()),
                        e -> String.valueOf(e.getValue())
                    ));
                    
        } catch (Exception e) {
            System.err.println("❌ Failed to fetch hash from Redis for key " + key + ": " + e.getMessage());
            return Collections.emptyMap();
        }
    }
}
