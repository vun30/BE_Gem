package online.gemfpt.BE.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class StallsSellNotFoundException extends RuntimeException {

    public StallsSellNotFoundException(String message) {
        super(message);
    }
     public StallsSellNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }


}
