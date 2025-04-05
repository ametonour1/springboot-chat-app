package com.chatapp.service;

import com.chatapp.model.User;
import com.chatapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public User registerUser(String username, String email, String password) {
        // Check if user already exists
        if (userRepository.findByUsername(username) != null) {
            throw new RuntimeException("Username already taken");
        }
        if (userRepository.findByEmail(email) != null) {
            throw new RuntimeException("Email already taken");
        }

        // Encrypt password before saving
        String encryptedPassword = passwordEncoder.encode(password);

        User user = new User(username, email, encryptedPassword);
        return userRepository.save(user);  // Save the user to the database
    }
}
