package com.protocol.supplychainx.common.exceptions.user;

public class EmailAlreadyExistsException extends RuntimeException {
    
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
    
    public EmailAlreadyExistsException(String email, boolean dummy) {
        super("Email already exists: " + email);
    }
}
