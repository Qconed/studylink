package dev.studylink.studylink.exception;

public class LoginError extends Exception {
    public LoginError(String message) {
        super(message);
    }

    public LoginError(String message, Throwable cause) {
        super(message, cause);
    }
}