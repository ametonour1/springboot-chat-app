package com.chatapp.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "group_message_status")
@IdClass(GroupMessageStatusId.class) // Tells JPA to use our composite key
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMessageStatus {

    @Id
    private Long groupChatId;

    @Id
    private Long userId;

    private Long lastReadMessageId;
}