package ru.codeportfolio.exceptions;

public class UncorrectRequestException extends RuntimeException{

    public UncorrectRequestException(String message) {
        super(message);
    }

    public UncorrectRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public UncorrectRequestException(Throwable cause) {
        super(cause);
    }
}
