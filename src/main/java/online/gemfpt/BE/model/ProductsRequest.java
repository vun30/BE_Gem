package online.gemfpt.BE.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Data
public class ProductsRequest {

    private String name;

    private String descriptions;

    private String category;

    private String oldID;

    @Min(0)
    private double priceRate;

    @NotBlank(message = "Barcode cannot be left blank")
    private String barcode;

    private List<ProductUrlRequest> urls;

    private List<MetalRequest> metals;

    private List<GemstoneRequest> gemstones;
}
