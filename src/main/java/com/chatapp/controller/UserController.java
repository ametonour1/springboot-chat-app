package com.chatapp.controller;

import com.chatapp.model.User;
import com.chatapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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

    @PostMapping("/request-password-reset")
    public ResponseEntity<String> requestPasswordReset(@RequestParam String email) {
        try {
            userService.requestPasswordReset(email);
            return ResponseEntity.ok("Password reset email sent successfully.");
        } catch (RuntimeException e) {
            // If user not found or any other error occurs
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        try {
            User user = userService.getUserByResetToken(token);
    
            // Check if the token has expired
            if (user.getResetTokenExpiration().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Reset token has expired");
            }
    
            // Add the token to the model so it can be used in the form
            model.addAttribute("token", token);
            return "resetPasswordForm"; // This is the view that will contain the form
        } catch (Exception e) {
            return "errorPage"; // A page for invalid or expired token
        }
    }

    @PostMapping("/update-password")
    public String updatePassword(@RequestParam("token") String token, @RequestParam("newPassword") String newPassword) {
        try {
            // Call UserService to reset the password
            userService.resetPassword(token, newPassword);
            return "passwordResetSuccess"; // return success page view
        } catch (RuntimeException e) {
            // If token is invalid or expired
            return "errorPage"; // return error page view
        }
    }
}
