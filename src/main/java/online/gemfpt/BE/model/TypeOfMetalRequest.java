package online.gemfpt.BE.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TypeOfMetalRequest {

    @NotBlank(message = "Metal type cannot be blank")
    private String metalType;

    @Min(value = 0, message = "Sell price must be non-negative")
    private double sellPrice;

    @Min(value = 0, message = "Buy price must be non-negative")
    private double buyPrice;
}
