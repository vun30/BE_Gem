package online.gemfpt.BE.Entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    String name;
    LocalDateTime CreateTime;
    LocalDateTime UpdateTime;
    String descriptions;
    String category;
    double price; // giá này là giá sau khi tính toán ( nhân với giá nguyên liệu + áp giá )
    double priceRate ;  // tỉ lệ áp giá
    int stock;
    String barcode;
    String url; // ảnh
    boolean Status;
}
