package com.chatapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
// Keep this for when you manually create it
@AllArgsConstructor 
public class UserSummaryDTO {

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotBlank(message = "Username cannot be blank")
    private String username;

    private boolean isAdmin;

    private String publicKey;

    // ADD THIS MANUAL CONSTRUCTOR for Hibernate
    public UserSummaryDTO(Long userId, String username, boolean isAdmin) {
        this.userId = userId;
        this.username = username;
        this.isAdmin = isAdmin;
        this.publicKey = null; // This will be hydrated later via Redis
    }
}