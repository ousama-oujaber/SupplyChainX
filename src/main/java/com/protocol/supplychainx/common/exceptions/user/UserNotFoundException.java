package com.protocol.supplychainx.common.exceptions.user;

public class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException(String message) {
        super(message);
    }
    
    public UserNotFoundException(Long id) {
        super("User not found with ID: " + id);
    }
}
