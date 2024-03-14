package com.wellnest.handleException;

public class EmailAlreadyRegisteredException extends RuntimeException {
    private String message;

    public EmailAlreadyRegisteredException(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
