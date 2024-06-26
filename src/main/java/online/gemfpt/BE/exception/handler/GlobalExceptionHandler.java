package online.gemfpt.BE.exception.handler;

import online.gemfpt.BE.exception.AccountNotFoundException;
import online.gemfpt.BE.exception.StallsSellNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentialsException(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<String> handleAccountNotFoundException(AccountNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Account not found" );
    }

     @ExceptionHandler(StallsSellNotFoundException .class)
    public ResponseEntity<String> handleStallsSellNotFoundException(StallsSellNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Stalls Sell Id Not Found");
    }

}
