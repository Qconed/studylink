package dev.studylink.studylink.exception;

public class UnauthorizedResourceAccessException extends Exception {
    public UnauthorizedResourceAccessException(String message) {
        super(message);
    }

    public UnauthorizedResourceAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}