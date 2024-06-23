package online.gemfpt.BE.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String customerName;
    private int customerPhone;
    private double totalAmount;
    private double discount;
    private String voucher;
    private LocalDateTime createTime;
    private boolean status;
    private String cashier;

    @OneToMany(mappedBy = "bill")
    private List<WarrantyCard> warrantyCards;

    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BillItem> items = new ArrayList<>();  // Khởi tạo danh sách

}
