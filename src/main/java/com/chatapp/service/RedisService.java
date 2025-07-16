package com.chatapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String ONLINE_STATUS_KEY = "user:online:";
    private static final String USER_SOCKET_KEY = "user:socket:";
    private static final String RECENT_CHATTERS_KEY_PREFIX = "recent_chatters_for_user:";

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
        return Boolean.parseBoolean(redisTemplate.opsForValue().get(ONLINE_STATUS_KEY + userId));
    }

    // Optional: Set user session expiry time in Redis
    public void setUserSessionExpiry(String userId, long minutes) {
        redisTemplate.expire(ONLINE_STATUS_KEY + userId, minutes, TimeUnit.MINUTES);
    }


    public void storeUserSocketSession(String userId, String sessionId) {
        redisTemplate.opsForValue().set(USER_SOCKET_KEY + userId, sessionId);
    }

    public String getUserSocketSession(String userId) {
        return redisTemplate.opsForValue().get(USER_SOCKET_KEY + userId);
    }

    public void deleteUserSocketSession(String userId) {
        redisTemplate.delete(USER_SOCKET_KEY + userId);
    }

    public void addRecentChatter(String userId, String chatterId) {

    String RECENT_CHATTERS_KEY = RECENT_CHATTERS_KEY_PREFIX + userId; 
        // Remove existing occurrences to avoid duplicates
        redisTemplate.opsForList().remove(RECENT_CHATTERS_KEY, 0, chatterId);
        // Push to front
        redisTemplate.opsForList().leftPush(RECENT_CHATTERS_KEY, chatterId);
        // Trim list to max 10 entries
        redisTemplate.opsForList().trim(RECENT_CHATTERS_KEY, 0, 10);
    }

    public List<String> getRecentChatters(String userId) {

        String RECENT_CHATTERS_KEY = RECENT_CHATTERS_KEY_PREFIX + userId; 
        // Get all recent chatters
        return redisTemplate.opsForList().range(RECENT_CHATTERS_KEY, 0, -1);
    }

}
