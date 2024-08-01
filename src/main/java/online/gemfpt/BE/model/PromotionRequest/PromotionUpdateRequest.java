package online.gemfpt.BE.model.PromotionRequest;

import jakarta.validation.constraints.*;
import lombok.Data;
import online.gemfpt.BE.enums.TypeEnum;

import java.time.LocalDateTime;

@Data
public class PromotionUpdateRequest {
    @NotBlank
    private String programName;

    @Min(0)
    @Max(100)
    private double discountRate;

    @NotBlank
    private String description;

    @NotBlank
    private String applicableProducts;

    private LocalDateTime endTime;
}
