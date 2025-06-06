package com.chatapp.exception;

public class UsernameTakenException extends BaseLocalizedException {
    @Override
    public String getMessageKey() {
        return "error.usernameTaken";
    }
}
