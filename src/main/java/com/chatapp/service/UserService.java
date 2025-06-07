package com.chatapp.service;

import com.chatapp.exception.UserExceptions;

import com.chatapp.model.User;
import com.chatapp.repository.UserRepository;
import com.chatapp.util.JwtUtil;
import com.chatapp.util.TranslationService;
import com.chatapp.service.RedisService;
import com.chatapp.util.EmailTemplateHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;


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

    @Autowired
    private TranslationService translationService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();


    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public User registerUser(String username, String email, String password, String lang) {
        // Check if user already exists
        if (userRepository.findByUsername(username).isPresent()) {
            throw new UserExceptions.UsernameTakenException();
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserExceptions.EmailTakenException();
        }
    
        String encryptedPassword = passwordEncoder.encode(password);
    
        User user = new User(username, email, encryptedPassword);
        
        String verificationToken = UUID.randomUUID().toString();
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(15); 

        user.setResetToken(verificationToken);
        user.setResetTokenExpiration(expiration);


        User savedUser = userRepository.save(user);

       String verificationLink = baseUrl + "/api/users/verify-email?token=" + verificationToken;

        String subject = EmailTemplateHelper.getVerificationEmailSubject(lang, translationService);
        Map<String, Object> content = EmailTemplateHelper.buildVerificationEmailContent(user, verificationLink, lang, translationService);
       emailService.sendHtmlEmail(
        user.getEmail(),
        subject,
        "verification-email-template",  // The template name
        content // Pass dynamic variables to the template
);

    // Return the saved user
    return savedUser;
    }
    
    public String loginUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
    
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

            // Check if the user's email is verified
            if (!user.isVerified()) {
            // Email not verified, check if the verification token has expired
                if (user.getResetTokenExpiration() != null && user.getResetTokenExpiration().isBefore(LocalDateTime.now())) {
                    // Token has expired, trigger a function to resend verification email
                    resendVerificationEmail(user);
                } else {
                    // Token has not expired, return a message to the user
                    throw new RuntimeException("Please verify your email before logging in.");
                }
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

    public void verifyEmail(String token) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
    
        // Check if the token has expired or is already used
        if (user.getResetTokenExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification token has expired.");
        }
    
        // Mark the user as verified
        user.setVerified(true);
        user.setResetToken(null); // Clear the token after verification
        user.setResetTokenExpiration(null); // Clear the expiration date as well
        userRepository.save(user); // Save the updated user
    }

    public void resendVerificationEmail(User user) {
        // Generate a new verification token
        String newVerificationToken = UUID.randomUUID().toString();
    
        // Set the expiration time (e.g., 24 hours from now)
        LocalDateTime newExpirationTime = LocalDateTime.now().plusHours(24);
    
        // Update the user record with the new token and expiration time
        user.setResetToken(newVerificationToken);
        user.setResetTokenExpiration(newExpirationTime);
    
        // Save the updated user to the database
        userRepository.save(user);
    
        // Send the verification email
        String verificationLink = "http://localhost:8080/api/users/verify-email?token=" + newVerificationToken;
        emailService.sendHtmlEmail(
                user.getEmail(),
                "Verify Your Email",
                "verification-email-template",  // The template name
                Map.of("username", user.getUsername(), "verificationLink", verificationLink)  // Pass dynamic variables to the template
        );
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
        String resetLink = "http://localhost:3000/api/users/reset-password?token=" + token;
    
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
        String encryptedPassword = passwordEncoder.encode(newPassword);
        
        // Update the user's password (you should hash the password before saving it)
        user.setPassword(encryptedPassword); // Ideally, use password hashing here (e.g., BCrypt)
        user.setResetToken(null); // Clear the reset token
        user.setResetTokenExpiration(null); // Clear the token expiration
        userRepository.save(user); // Save the updated user
    }

}
