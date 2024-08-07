package online.gemfpt.BE.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import online.gemfpt.BE.enums.TypeEnum;
import online.gemfpt.BE.enums.TypeOfProductEnum;

import java.util.List;

@Data
public class ProductsRequest {

    private String name;

    private String descriptions;

    private TypeEnum category;

    private TypeOfProductEnum typeWhenBuyBack;

    @NotNull
    private long stallId;


    @Min(0)
    private double priceRate;

    private double wage;

    @NotBlank(message = "Barcode cannot be left blank")
    private String barcode;

    private List<ProductUrlRequest> urls;

    private List<MetalRequest> metals;

    private List<GemstoneRequest> gemstones;
}
