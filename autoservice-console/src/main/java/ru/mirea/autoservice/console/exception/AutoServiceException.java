package ru.mirea.autoservice.console.exception;

public class AutoServiceException extends RuntimeException {

    public AutoServiceException(String message) {
        super(message);
    }

    public AutoServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
