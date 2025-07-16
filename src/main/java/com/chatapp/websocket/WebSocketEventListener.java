package com.chatapp.websocket;

import com.chatapp.service.RedisService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private final RedisService redisService;

    public WebSocketEventListener(RedisService redisService) {
        this.redisService = redisService;
    }

    // @EventListener
    // public void handleWebSocketConnectListener(SessionConnectEvent event) {
    //     StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
    //     String userId = accessor.getFirstNativeHeader("userId");

    //     if (userId != null) {
    //         accessor.getSessionAttributes().put("userId", userId);
    //         redisService.setUserOnline(userId);  // set status with TTL
    //         redisService.saveUserSocketSession(userId, accessor.getSessionId());
    //         System.out.println("User " + userId + " connected via WebSocket.");
    //     }
    // }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String userId = (String) accessor.getSessionAttributes().get("userId");

        if (userId != null) {
            redisService.deleteUserSocketSession(userId);
            redisService.markUserOffline(userId);
            System.out.println("User " + userId + " disconnected. Set offline in Redis.");
        }
    }
}