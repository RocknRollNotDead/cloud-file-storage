package ru.codeportfolio.exceptions;

public class SelfRatingException extends RuntimeException {
    public SelfRatingException(String message) {
        super(message);
    }
    public SelfRatingException(String message, Throwable cause) {
        super(message, cause);
    }
}
