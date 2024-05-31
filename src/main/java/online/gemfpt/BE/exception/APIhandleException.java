package online.gemfpt.BE.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLClientInfoException;
import java.sql.SQLIntegrityConstraintViolationException;

@RestControllerAdvice // canh nhung thang co loi no se bat ngay lap tuc
public class APIhandleException {
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleNotAllowException(BadCredentialsException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<Object> handleNotAllowException(SQLIntegrityConstraintViolationException ex) {
        return new ResponseEntity<>(("Duplicate phone number"), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Object> handleDuplicatePhone(AuthException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }}