package ru.codeportfolio.exceptions;

import org.hibernate.NonUniqueResultException;

public class CannotFindNessesaryEntity extends RuntimeException {

    public CannotFindNessesaryEntity(String message) {
        super(message);
    }

    public CannotFindNessesaryEntity(String message, Throwable cause) {
        super(message, cause);
    }

    public CannotFindNessesaryEntity(Throwable cause) {
        super(cause);
    }
}
