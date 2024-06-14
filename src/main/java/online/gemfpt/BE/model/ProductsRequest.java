package online.gemfpt.BE.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import online.gemfpt.BE.entity.ProductUrl;

import java.util.List;

@Data
public class ProductsRequest {
    @NotBlank(message = "Name cannot be left blank")
    private String name;

    @NotBlank(message = "Descriptions cannot be left blank")
    private String descriptions;

    @NotBlank(message = "Category cannot be left blank")
    private String category;

    @Min(value = 0, message = "Price must be = or more than 0")
    private double price;

    private double priceRate;

    @Min(value = 0, message = "Must be at least 1 product in stock")
    private int stock;

    @NotBlank(message = "Barcode cannot be left blank")
    private String barcode;

    private List<ProductUrl> urls;

    private List<MetalRequest> metals;
    private List<GemstoneRequest> gemstones;
}
