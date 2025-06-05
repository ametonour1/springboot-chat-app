package com.chatapp.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import com.chatapp.dto.Message;

@Controller
public class MessageController {

    @MessageMapping("/send")
    public void handleMessage(Message message) {
        System.out.println("Received message from: " + message.getFrom());
        System.out.println("Content: " + message.getContent());
    }
}
