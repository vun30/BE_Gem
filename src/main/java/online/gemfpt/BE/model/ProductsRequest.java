package online.gemfpt.BE.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ProductsRequest { // 2 list for  fe canbe input one orr many metals or gem
    @NotBlank(message = "Name cannot be left blank")
    private String name;

    @NotBlank(message = "Descriptions cannot be left blank")
    private String descriptions;

    @NotBlank(message = "Category cannot be left blank")
    private String category;

    @Min(value = 1, message = "Price must be more than 0")
    private double price;

    private double priceRate;

    @Min(value = 1, message = "Must be at least 1 product in stock")
    private int stock;

    @NotBlank(message = "Barcode cannot be left blank")
    private String barcode;

    private String url;

    private List<MetalRequest> metals;
    // 2 list for  fe canbe input one orr many metals or gem

    private List<GemstoneRequest> gemstones;
}