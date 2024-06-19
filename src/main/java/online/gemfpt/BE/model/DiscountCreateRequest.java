package online.gemfpt.BE.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DiscountCreateRequest {

    @NotBlank
    private String programName;

    @Min(0)
    @Max(100)
    private double discountRate;

    @NotBlank
    private String description;

    private LocalDateTime endTime;
}