package ru.codeportfolio.exceptions;

public class ValidationException extends RuntimeException{
    public ValidationException(String message, Exception e){
        super(message, e);
    }
    public ValidationException(String message){
        super(message);
    }

}
