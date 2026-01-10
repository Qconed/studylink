package dev.studylink.studylink.exception;

public class InvalidResourceDataException extends Exception {
    public InvalidResourceDataException(String message) {
        super(message);
    }

    public InvalidResourceDataException(String message, Throwable cause) {
        super(message, cause);
    }
}