package online.gemfpt.BE.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long productId;

    @NotBlank
    String name;

    LocalDateTime createTime;
    LocalDateTime updateTime;

    String descriptions;

    String category;

    @Min(0)
    double price; // giá này là giá sau khi tính toán ( nhân với giá nguyên liệu + áp giá )

    @Min(0) @Max(100)
    double priceRate;  // tỉ lệ áp giá

    @Min(0)
    int stock;

    @NotBlank
    String barcode;

    String url; // ảnh

    boolean status;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Gemstone> gemstones;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Metal> metals;

    @ManyToMany(mappedBy = "product")
    private List<DiscountProduct> discountProducts;
}
