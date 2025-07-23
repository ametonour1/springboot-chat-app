package com.chatapp.websocket;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.List;
import java.util.Map;

public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // Extract userId from query params (e.g., /ws?userId=123)
        URI uri = request.getURI();
        String query = uri.getQuery();
        String userId = null;

        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2 && keyValue[0].equals("userId")) {
                    userId = keyValue[1];
                    break;
                }
            }
        }

        if (userId == null || userId.isEmpty()) {
            System.out.println("UserId not provided, rejecting websocket handshake");
            return false; // Reject handshake if no userId
        }

        // Store userId for later use
        attributes.put("userId", userId);
        System.out.println("Handshake accepted for userId: " + userId);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // No action needed after handshake
    }
}
