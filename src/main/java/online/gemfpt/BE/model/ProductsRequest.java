package online.gemfpt.BE.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

@Data
public class ProductsRequest {
    @NotBlank(message = "Name cannot be left blank")
    @NotNull
    String name;
    @NotBlank(message = "Descriptions cannot be left blank")
    @NotNull
    String descriptions;
    @NotBlank(message = "Category cannot be left blank")
    @NotNull
    String category;
    @Min(value = 1, message = "Price must more than 0")
    double price; // giá này là giá sau khi tính toán ( nhân với giá nguyên liệu + áp giá )
    double priceRate ;  // tỉ lệ áp giá
    @Min(value = 1, message = "Must be at least 1 product in stock")
    int stock;
    @NotBlank(message = "Barcode cannot be left blank")
    @NotNull
    String barcode;
    String url; // ảnh
//    boolean Status = true;
}
