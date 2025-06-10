package com.chatapp.controller;

import com.chatapp.model.User;
import com.chatapp.service.UserService;
import com.chatapp.validation.UserValidators;
import com.chatapp.util.TranslationService;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;


import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;
import com.chatapp.dto.LoginRequest;
import com.chatapp.dto.LoginResponseDTO;
import com.chatapp.exception.UserExceptions;
import com.chatapp.exception.UserExceptions.InvalidVerificationTokenException;



@RestController
@RequestMapping("/api/users")  // Base URL for User endpoints
public class UserController {

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    @Autowired
    private UserService userService;
    private final TranslationService translationService;

    public UserController(UserService userService, TranslationService translationService) {
        this.userService = userService;
        this.translationService = translationService;
    }

    // Register a new user
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody User user, @RequestHeader(value = "Accept-Language", defaultValue = "en") String lang) {
            UserValidators.RegisterValidator.validate(user);
            User registeredUser = userService.registerUser(user.getUsername(), user.getEmail(), user.getPassword(), lang);
            String message = translationService.getTranslation(lang, "success.userRegisteredWithEmail");

            return ResponseEntity.ok(Map.of("message", message));
  
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, @RequestHeader(value = "Accept-Language", defaultValue = "en") String lang) {

            UserValidators.LoginValidator.validate(request);
            // Get user details to include in response
            User user = userService.getUserByEmail(request.getEmail());
    
            // Use existing loginUser method to validate and return token
            String token = userService.loginUser(request.getEmail(), request.getPassword());
    
            return ResponseEntity.ok(new LoginResponseDTO(token, user.getId(), user.getEmail(), user.getUsername()));
  
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

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token, @RequestParam(value = "lang", defaultValue = "en") String lang) {
    try {
        userService.verifyEmail(token); // Call the service to verify the token
       String redirectUrl = frontendBaseUrl + "/verify-success?lang=" + lang;
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    } catch (InvalidVerificationTokenException e) {
          String redirectUrl = frontendBaseUrl + "/verify-failed?lang=" + lang;
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }
}

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestParam("email") String email,
                                                @RequestParam(value = "lang", defaultValue = "en") String lang) {
      
        userService.resendVerificationEmail(email, lang);
        String message = translationService.getTranslation(lang, "user.verification.emailSent");

        return ResponseEntity.ok(Map.of("message", message));


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
    public ResponseEntity<?> validateResetToken(@RequestParam("token") String token) {
        try {
            User user = userService.getUserByResetToken(token);

            // Check if the token has expired
            if (user.getResetTokenExpiration().isBefore(LocalDateTime.now())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Reset token has expired"));
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Token is valid",
                    "token", token
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid or expired token"));
        }
    }

    @PostMapping("/update-password")
    public ResponseEntity<?> updatePassword(@RequestParam("token") String token,
                                            @RequestParam("newPassword") String newPassword) {
        try {
            userService.resetPassword(token, newPassword);
            return ResponseEntity.ok(Map.of("message", "Password has been successfully reset"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
