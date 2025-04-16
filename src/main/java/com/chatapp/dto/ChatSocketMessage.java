package com.chatapp.dto;

public class ChatSocketMessage {
    private String to;
    private String text;

    // Constructors
    public ChatSocketMessage() {}

    public ChatSocketMessage(String to, String text) {
        this.to = to;
        this.text = text;
    }

    // Getters and Setters
    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
