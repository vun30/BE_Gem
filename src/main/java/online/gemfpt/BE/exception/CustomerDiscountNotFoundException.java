package online.gemfpt.BE.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CustomerDiscountNotFoundException extends RuntimeException {
    public CustomerDiscountNotFoundException(String phone, Long discountId) {
        super("Customer with phone number " + phone + " hasn't applied discount with this discount id: " + discountId);
    }
}
