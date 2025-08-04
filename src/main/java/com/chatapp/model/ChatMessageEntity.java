package com.chatapp.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "chat_messages")
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false)
    private Long recipientId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status = MessageStatus.SENT;

     @Column(columnDefinition = "TEXT")
    private String encryptedAESKeyForSender;

    @Column(columnDefinition = "TEXT")
    private String encryptedAESKeyForRecipient;

    private String iv;

    public ChatMessageEntity(Long senderId,
                            Long recipientId,
                            String content,
                            MessageStatus status,
                            String encryptedAESKeyForSender,
                            String encryptedAESKeyForRecipient,
                            String iv) {
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.content = content;
        this.status = status;
        this.encryptedAESKeyForSender = encryptedAESKeyForSender;
        this.encryptedAESKeyForRecipient = encryptedAESKeyForRecipient;
        this.iv = iv;
    }

}
