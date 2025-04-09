package com.chatapp.controller;

import com.chatapp.model.User;
import com.chatapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import com.chatapp.dto.LoginRequest;



@RestController
@RequestMapping("/api/users")  // Base URL for User endpoints
public class UserController {

    @Autowired
    private UserService userService;

    // Register a new user
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user.getUsername(), user.getEmail(), user.getPassword());
            return ResponseEntity.ok("User registered successfully: " + registeredUser.getUsername());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error registering user: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Get user details to include in response
            User user = userService.getUserByEmail(request.getEmail());
    
            // Use existing loginUser method to validate and return token
            String token = userService.loginUser(request.getEmail(), request.getPassword());
    
            return ResponseEntity.ok(Map.of(
                "token", token,
                "userId", user.getId(),
                "email", user.getEmail(),
                "username", user.getUsername()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
    

    @PostMapping("/logout")
    public String logoutUser(@RequestParam String userId) {
        userService.logoutUser(userId);
        return "User logged out successfully!";
    }

    @GetMapping("/status")
    public boolean checkUserStatus(@RequestParam String userId) {
        return userService.checkUserOnlineStatus(userId);
    }
}
