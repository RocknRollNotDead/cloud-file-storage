package ru.codeportfolio.controllers.other;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.codeportfolio.exceptions.AlreadyExistException;
import ru.codeportfolio.exceptions.DataAccessException;
import ru.codeportfolio.exceptions.NotFoundException;
import ru.codeportfolio.exceptions.ValidationException;

import java.util.Map;

@ControllerAdvice
public class MyExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(MyExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleGeneric(NotFoundException e) {
        return buildResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<Map<String, String>> handleGeneric(AlreadyExistException e) {
        return buildResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleGeneric(ValidationException e) {
        return buildResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String, String>> handleGeneric(DataAccessException e) {
        log.error(e.getMessage().toUpperCase(), e);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Database error!");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleGeneric(RuntimeException e) {
        log.error(e.getMessage().toUpperCase(), e);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Backend error!");
    }


    private ResponseEntity<Map<String, String>> buildResponse(HttpStatus status, String message) {
        Map<String, String> body = Map.of(
                "message", message);
        return ResponseEntity.status(status).body(body);
    }

}
