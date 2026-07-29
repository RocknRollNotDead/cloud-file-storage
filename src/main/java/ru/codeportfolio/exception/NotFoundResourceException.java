package ru.codeportfolio.exception;

public class NotFoundResourceException extends RuntimeException {
    public NotFoundResourceException(String message) {
        super(message);
    }

    public NotFoundResourceException() {
    }
}
