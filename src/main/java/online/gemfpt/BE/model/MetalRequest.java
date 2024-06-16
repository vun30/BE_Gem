package online.gemfpt.BE.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MetalRequest {
    @NotBlank(message = "Metal name cannot be left blank")
    private String name;

    private String description;

    @Min(value = 0, message = "Weight must be >= 0")
    private double weight;

}
