package com.chatapp.dto;

public class ChatSocketTestMessage {
    private String text;

    public ChatSocketTestMessage() {}
    public ChatSocketTestMessage(String text) { this.text = text; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}