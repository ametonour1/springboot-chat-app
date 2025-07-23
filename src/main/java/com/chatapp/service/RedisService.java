package com.chatapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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

    public List<String> getRecentChatters(String userId) {

        String RECENT_CHATTERS_KEY = RECENT_CHATTERS_KEY_PREFIX + userId; 
        // Get all recent chatters
        return redisTemplate.opsForList().range(RECENT_CHATTERS_KEY, 0, -1);
    }

}
