package com.chatapp.service;

import com.chatapp.model.User;
import com.chatapp.repository.UserRepository;
import com.chatapp.util.JwtUtil;
import com.chatapp.service.RedisService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisService redisService;

    @Autowired
    private EmailService emailService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public User registerUser(String username, String email, String password) {
        // Check if user already exists
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already taken");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already taken");
        }
    
        // Encrypt password before saving
        String encryptedPassword = passwordEncoder.encode(password);
    
        User user = new User(username, email, encryptedPassword);
        return userRepository.save(user); // Save the user to the database
    }
    
    public String loginUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
    
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
    
        redisService.markUserOnline(user.getId().toString());
        
        return jwtUtil.generateToken(user); // Return JWT
    }

    public void logoutUser(String userId) {
        // Additional logout logic (e.g., invalidate session)
        redisService.markUserOffline(userId);
    }

    public boolean checkUserOnlineStatus(String userId) {
        return redisService.isUserOnline(userId);
    }

    public String getUsernameByEmail(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            return optionalUser.get().getUsername();
        } else {
            throw new RuntimeException("User not found with email: " + email);
        }
    }

    
    public User getUserById(String userId) {
        try {
            Long id = Long.parseLong(userId); // Convert String to Long
            Optional<User> user = userRepository.findById(id);
            return user.orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid user ID format: " + userId, e);
        }
    }
    
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    
        // Generate reset token and expiration
        String token = UUID.randomUUID().toString();
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(15); // 15 min expiration
    
        // Save reset token and expiration to the user
        user.setResetToken(token);
        user.setResetTokenExpiration(expiration);
        userRepository.save(user);
    
        // Prepare the reset password link
        String resetLink = "http://localhost:3000/reset-password?token=" + token;
    
        // Send the password reset email
        emailService.sendHtmlEmail(
            user.getEmail(),
            "Reset Your Password",
            "reset-password-template",  // The template name
            Map.of("username", user.getUsername(), "resetLink", resetLink)  // Pass dynamic variables to the template
    );
    }

    public User getUserByResetToken(String token) throws RuntimeException {
        // Fetch the user by the reset token
        return userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));
    }

    public void resetPassword(String token, String newPassword) {
        // Find the user by reset token
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token"));
        
        // Check if the token has expired
        if (user.getResetTokenExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired");
        }
        
        // Update the user's password (you should hash the password before saving it)
        user.setPassword(newPassword); // Ideally, use password hashing here (e.g., BCrypt)
        user.setResetToken(null); // Clear the reset token
        user.setResetTokenExpiration(null); // Clear the token expiration
        userRepository.save(user); // Save the updated user
    }

}
