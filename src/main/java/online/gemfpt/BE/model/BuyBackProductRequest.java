package online.gemfpt.BE.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import online.gemfpt.BE.enums.TypeEnum;

import java.util.List;

@Data
public class BuyBackProductRequest {
    private String name;

    private String descriptions;

    private TypeEnum category;


//    @Min(0)
//    private double priceBuyRate;

    @NotBlank(message = "Barcode cannot be left blank")
    private String barcode;

    private List<ProductUrlRequest> urls;

    private List<MetalRequest> metals;

    private List<GemstoneRequest> gemstones;
}
