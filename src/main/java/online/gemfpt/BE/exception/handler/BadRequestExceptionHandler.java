package online.gemfpt.BE.exception.handler;

import online.gemfpt.BE.exception.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class BadRequestExceptionHandler {
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleInvalidUsernamePassword(BadRequestException badRequestException){
return new ResponseEntity(badRequestException.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
