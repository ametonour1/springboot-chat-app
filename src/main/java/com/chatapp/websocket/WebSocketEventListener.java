package com.chatapp.websocket;

import com.chatapp.service.RedisService;
import com.chatapp.service.SessionTracker;

import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private final RedisService redisService;
    private final SessionTracker sessionTracker;

    public WebSocketEventListener(RedisService redisService, SessionTracker sessionTracker) {
        this.redisService = redisService;
        this.sessionTracker = sessionTracker;
    }


    // @EventListener
    // public void handleSessionConnected(SessionConnectedEvent event) {
    //     StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
    //     String sessionId = accessor.getSessionId();

    //     Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
    //     System.out.println("Session attributes: " + sessionAttributes);


    //     if (sessionAttributes != null && sessionAttributes.get("userId") != null) {
    //         String userId = (String) sessionAttributes.get("userId");
    //         System.out.println("User " + userId + " registered with session id " + sessionId);

    //         // redisService.storeUserSocketSession(userId, sessionId);
    //         // ...
    //     } else {
    //         System.out.println("No userId found on connect for session " + sessionId);
    //     }
    // }

      @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String userId = (String) sha.getSessionAttributes().get("userId");
        String sessionId = sha.getSessionId();

        if (userId != null) {
            //sessionTracker.storeUserSocketSession(userId, sessionId);
            System.out.println("User connected: " + userId + " with session: " + sessionId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
         StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
    String sessionId = accessor.getSessionId();


    String userId = redisService.getUserIdFromSession(sessionId);

    if (userId != null) {

        redisService.removeUserSocketSession(userId, sessionId);

        redisService.removeSessionIdMapping(sessionId);
       

    
        Long remainingConnections = redisService.getUserConnectionCount(userId);

        if (remainingConnections == 0) {
            redisService.updateUserLastSeen(userId);
            System.out.println("User " + userId + " fully disconnected and marked offline.");
        } else {
            System.out.println("User " + userId + " disconnected one session, still active on others.");
        }
    } else {
        System.out.println("No userId found for session " + sessionId);
    }
    }
}