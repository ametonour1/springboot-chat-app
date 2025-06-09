package com.chatapp.exception;

public abstract class BaseLocalizedException extends RuntimeException {
    public abstract String getMessageKey();
}
