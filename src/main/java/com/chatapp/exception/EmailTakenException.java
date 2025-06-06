package com.chatapp.exception;

public class EmailTakenException extends RuntimeException {
    public EmailTakenException() {
        super("Email already taken");
    }
}