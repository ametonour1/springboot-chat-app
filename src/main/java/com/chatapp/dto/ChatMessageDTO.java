package com.chatapp.dto;

import lombok.Data;

@Data
public class ChatMessageDTO {
    private String messageId;
    private String from;
    private String to;
    private String text;
    private Long timestamp;
}
