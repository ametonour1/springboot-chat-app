package com.chatapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

import com.chatapp.websocket.UserHandshakeHandler;
import com.chatapp.websocket.WebSocketHandshakeInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // Client can subscribe to /topic
        config.setApplicationDestinationPrefixes("/app"); // Client sends to /app
         config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").addInterceptors(new WebSocketHandshakeInterceptor()).setHandshakeHandler(new UserHandshakeHandler()).setAllowedOriginPatterns("http://localhost:3000").withSockJS();
    }
}
