package online.gemfpt.BE.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class BillItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "bill_id")
    private Bill bill;

//    @ManyToOne
//    @JoinColumn(name = "product_barcode")
    private String product_barcode;

    private int quantity;
    private double price;
}
