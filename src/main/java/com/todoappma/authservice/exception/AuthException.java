package com.todoappma.authservice.exception;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {

    private final String errorCode;

    public AuthException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public static AuthException invalidCredentials() {
        return new AuthException("AUTH_001", "Invalid email or password");
    }

    public static AuthException emailAlreadyTaken() {
        return new AuthException("AUTH_002", "An account with this email already exists");
    }
}
