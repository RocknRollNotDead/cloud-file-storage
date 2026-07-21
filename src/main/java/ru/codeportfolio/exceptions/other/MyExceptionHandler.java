package ru.codeportfolio.exceptions.other;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.codeportfolio.exceptions.AlreadyExistException;
import ru.codeportfolio.exceptions.NotFoundException;
import ru.codeportfolio.exceptions.ValidationException;

import java.util.Map;

@ControllerAdvice
@Slf4j
public class MyExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleGeneric(NotFoundException e) {
        log.debug("handle err 404");
        return buildResponse(HttpStatus.NOT_FOUND, e.getMessage());

    }

    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<Map<String, String>> handleGeneric(AlreadyExistException e) {
        return buildResponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleGeneric(ValidationException e) {
        log.debug("debug handle " + e);
        return buildResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleGeneric(BadCredentialsException e){

        return buildResponse(HttpStatus.UNAUTHORIZED, "Not right login or password!");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuth(AuthenticationException e) {
        log.debug("debug handle " + e);
        return buildResponse(HttpStatus.UNAUTHORIZED, "User not authorized!");//e.getMessage()

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
