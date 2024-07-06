package online.gemfpt.BE.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BillRequest {
    @NotEmpty(message = "Phone number cannot be empty")
    private String customerPhone;

    private long discountId;

    @NotEmpty(message = "Barcodes cannot be empty")
    private List<String> barcodes;


}