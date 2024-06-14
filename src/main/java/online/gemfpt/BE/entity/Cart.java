package online.gemfpt.BE.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String type;
    private double total;
    private double discount;
    private String voucher;
    private LocalDateTime createTime;
    private boolean status = true;
    private int cusPhone;
    private String proBarcode;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "barcode")
    private Product product;

}
