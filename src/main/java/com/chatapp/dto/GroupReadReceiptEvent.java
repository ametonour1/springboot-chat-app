package com.chatapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO representing a user reading messages up to a certain point in a GROUP chat.
 * Separated from 1v1 logic to prevent mapping overlaps and keep code isolated.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupReadReceiptEvent {

  
    @Builder.Default
    private String type = "GROUP_READ_RECEIPT";

    private Long userId;

    private Long groupChatId;

    private Long lastReadMessageId;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}