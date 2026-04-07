package com.chatapp.model;


import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class GroupMessageStatusId implements Serializable {
    private Long groupChatId;
    private Long userId;

    // Equals and HashCode are required for composite keys in JPA
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupMessageStatusId that = (GroupMessageStatusId) o;
        return Objects.equals(groupChatId, that.groupChatId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupChatId, userId);
    }
}