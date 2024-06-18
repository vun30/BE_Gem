package online.gemfpt.BE.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Data

public class DiscountRequest {
    @NotNull
    private long disID;

    @NotBlank
    private String programName;

    @Min(0)
    @Max(100)
    private double discountRate;

    @NotBlank
    String description;

    @NotBlank
    private String applicableProducts;

    @NotBlank
    private String pointsCondition;

    private LocalDateTime endTime;

    @NotEmpty
    private List<String> barcode;
}
