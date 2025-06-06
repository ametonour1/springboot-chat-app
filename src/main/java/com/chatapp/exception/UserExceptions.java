package com.chatapp.exception;

import com.chatapp.exception.BaseLocalizedException;


public class UserExceptions {

    public static class MissingUsernameException extends BaseLocalizedException {
        @Override
        public String getMessageKey() {
            return "error.missingUsername";
        }
    }

    public static class MissingPasswordException extends BaseLocalizedException {
        @Override
        public String getMessageKey() {
            return "error.missingPassword";
        }
    }

    // Add other user exceptions here similarly
}
