package online.gemfpt.BE.Entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    String name;
    Date CreateTime;
    String descriptions;
    String category;
    double price; // giá này là giá sau khi tính toán ( nhân với giá nguyên liệu + áp giá )
    double priceRate ;  // tỉ lệ áp giá
    int stock;
    String url; // ảnh
    String Status;
}
