package com.chatapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users") // Table name in PostgreSQL
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Auto-incremented Primary Key

    @Column(nullable = false, unique = true, length = 50)
    private String username;  // Unique username

    @Column(nullable = false, unique = true, length = 100)
    private String email;  // Unique email

    @Column(nullable = false)
    private String password;  // Hashed password (store securely!)

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();  // Timestamp for user creation

    // Constructor (optional)
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
