package com.chatapp.controller;


import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import com.chatapp.dto.ChatSocketTestMessage;


@Controller
public class ChatTestController {

    @MessageMapping("/chat/test")
    @SendTo("/topic/test")
    public ChatSocketTestMessage handleTest(@Payload ChatSocketTestMessage message) {
        System.out.println("Received in test controller: " + message.getText());
        return message;
    }
}