package online.gemfpt.BE.model;

import lombok.Data;

import java.util.Date;

@Data
public class ProductsRequest {

    String name;
    Date CreateTime;
    String descriptions;
    String category;
    double price; // giá này là giá sau khi tính toán ( nhân với giá nguyên liệu + áp giá )
    double priceRate ;  // tỉ lệ áp giá
    int stock;
    String barcode;
    String url; // ảnh
    boolean Status = true;


}
