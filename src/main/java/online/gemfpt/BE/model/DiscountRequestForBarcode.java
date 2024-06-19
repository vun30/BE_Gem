package online.gemfpt.BE.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import online.gemfpt.BE.enums.TypeEnum;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DiscountRequestForBarcode {
    @NotBlank
    private String programName;

    @Min(0)
    @Max(100)
    private double discountRate;

    @NotBlank
    private String description;

    @NotBlank
    private String applicableProducts;

    @NotBlank
    private String pointsCondition;

    private LocalDateTime endTime;

    @NotEmpty
    private List<String> barcode;
}