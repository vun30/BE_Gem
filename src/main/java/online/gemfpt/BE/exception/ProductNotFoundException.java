package online.gemfpt.BE.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException() {
        super("No products found!");
    }

    public ProductNotFoundException(String message) {
        super(message);
    }
}
