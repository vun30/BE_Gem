package online.gemfpt.BE.model.PromotionRequest;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Data
public class PromotionRequestForBarcode {
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

    @NotEmpty
    private List<String> barcode;
}
