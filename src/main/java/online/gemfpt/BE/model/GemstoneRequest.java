package online.gemfpt.BE.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GemstoneRequest {
    @NotBlank(message = "Description cannot be left blank")
    private String description;

    @Min(value = 0, message = "Price must be more than 0")
    private double price;

    @Min(value = 0, message = "Quantity must be more than 0")
    private int quantity;
}
