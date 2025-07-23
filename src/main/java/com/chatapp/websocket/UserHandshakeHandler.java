package com.chatapp.websocket;

import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

public class UserHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(org.springframework.http.server.ServerHttpRequest request,
                                      org.springframework.web.socket.WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        String userId = (String) attributes.get("userId");
        return userId != null ? new Principal() {
            @Override
            public String getName() {
                return userId;
            }
        } : null;
    }
}
