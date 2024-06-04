package online.gemfpt.BE.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MetalRequest {
    @NotBlank(message = "Metal name cannot be left blank")
    private String name;

    private String description;

    @Min(value = 1, message = "Weight must be more than 0")
    private double weight;

    @Min(value = 1, message = "Price per weight unit must be more than 0")
    private double pricePerWeightUnit;

    @NotBlank(message = "Unit cannot be left blank")
    private String unit;
}
