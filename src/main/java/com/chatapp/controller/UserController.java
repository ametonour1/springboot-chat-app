package com.chatapp.controller;

import com.chatapp.model.User;
import com.chatapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
