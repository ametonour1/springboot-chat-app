package com.chatapp.model;


import javax.persistence.*;
import java.time.Instant;


@Entity
@Table(name = "user_last_seen")
public class UserLastSeen {

    @Id
    @Column(name = "user_id", nullable = false)
    private long userId;

    @Column(name = "is_online", nullable = false)
    private boolean isOnline;

    @Column(name = "last_seen", nullable = false)
    private Instant lastSeen;

    public UserLastSeen() {}

    public UserLastSeen(long userId, boolean isOnline, Instant lastSeen) {
        this.userId = userId;
        this.isOnline = isOnline;
        this.lastSeen = lastSeen;
    }

    // Getters and setters

    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }

    public boolean isOnline() {
        return isOnline;
    }
    public void setOnline(boolean online) {
        isOnline = online;
    }

    public Instant getLastSeen() {
        return lastSeen;
    }
    public void setLastSeen(Instant lastSeen) {
        this.lastSeen = lastSeen;
    }
}
