package com.chatapp.validation;

import com.chatapp.dto.LoginRequest;
import com.chatapp.exception.UserExceptions;
import com.chatapp.model.User;

public class UserValidators {

    public static class RegisterValidator {
        public static void validate(User user) {
            if (user.getUsername() == null || user.getUsername().isBlank()) {
                throw new UserExceptions.MissingUsernameException();
            }
            if (user.getEmail() == null || user.getEmail().isBlank()) {
                throw new UserExceptions.MissingEmailException();
            }
            if (user.getPassword() == null || user.getPassword().isBlank()) {
                throw new UserExceptions.MissingPasswordException();
            }
        }
    }

    // You can later add more like:
      public static class LoginValidator {
        public static void validate(LoginRequest request) {
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                throw new UserExceptions.MissingEmailException();
            }
            if (request.getPassword() == null || request.getPassword().isBlank()) {
                throw new UserExceptions.MissingPasswordException();
            }
            // Optionally: Add regex/email format checks here if needed
        }
    }
}