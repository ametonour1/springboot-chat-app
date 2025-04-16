package com.chatapp.util;

import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.TextMessage;

public class MyWebSocketHandler extends TextWebSocketHandler {

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Handle incoming messages here
        System.out.println("Received message: " + message.getPayload());

        // Send response to client
        session.sendMessage(new TextMessage("Message received"));
    }
}
