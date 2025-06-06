package com.chatapp.exception;

public class EmailTakenException extends BaseLocalizedException {
    @Override
    public String getMessageKey() {
        return "error.emailTaken";
    }
}
