package online.gemfpt.BE.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GemstoneRequest {

    private String description;

    @Min(value = 0, message = "Price must be more than 0")
    private double price;

    @Min(value = 0, message = "Quantity must be more than 0")
    private int quantity;

    @NotBlank(message = "Certificate Code cannot be left blank")
    private String certificateCode;

    private double carat;

    private String color;

    private String clarity;

    private String cut;



}
