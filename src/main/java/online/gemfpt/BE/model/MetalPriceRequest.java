package online.gemfpt.BE.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MetalPriceRequest {
    @NotBlank(message = "Metal name cannot be left blank")
    private String name;

    private String description;

    @Min(value = 1, message = "Weight must be more than 0")
    private double weight;

    @NotBlank(message = "Unit cannot be left blank")
    private double unit = 3.75;

    @NotBlank(message = "Metal type cannot be left blank")
    private String type; // thêm trường type để xác định loại kim loại
}
