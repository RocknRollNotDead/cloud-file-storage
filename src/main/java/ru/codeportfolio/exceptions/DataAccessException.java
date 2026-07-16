package ru.codeportfolio.exceptions;

import java.sql.SQLException;

public class DataAccessException extends RuntimeException{
    public DataAccessException(String message, RuntimeException e) {
        super(message, e);
    }

    public DataAccessException(RuntimeException e){
        super(e);
    }

    public DataAccessException(String message) {
        super(message);
    }
}
