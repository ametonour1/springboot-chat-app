package com.chatapp.config;

import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.stereotype.Component;

@Component
public class WebSocketInboundInterceptor implements ChannelInterceptor {
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // Log message type and payload content
        System.out.println("Message Type: " + message.getClass().getName());  // Log the message type
        if (message.getPayload() instanceof byte[]) {
            byte[] payload = (byte[]) message.getPayload();
            System.out.println("Payload Content: " + new String(payload));  // Log the payload as a string
        }

        return message;  // Continue the flow
    }
}
