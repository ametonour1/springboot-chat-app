package com.chatapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix for sending messages from the client (e.g., @MessageMapping)
        System.out.println("BROKER HIT");

        registry.setApplicationDestinationPrefixes("/app");
        
        // Enable a simple in-memory broker for topic-based communication
        registry.enableSimpleBroker("/topic");  // This will handle broadcasting to subscribers
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the WebSocket endpoint, where clients will connect
        registry.addEndpoint("/chat").setAllowedOrigins("http://127.0.0.1:5500").withSockJS();
        // Enables SockJS fallback if WebSockets aren't supported
    }
}
