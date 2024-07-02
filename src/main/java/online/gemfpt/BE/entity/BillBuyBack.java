package online.gemfpt.BE.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import online.gemfpt.BE.enums.TypeBillEnum;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class BillBuyBack {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;


    private TypeBillEnum typeBill; // them moi

    private String customerName;

    @Pattern(regexp = "^(\\+84|0)\\d{9,10}$", message = "Invalid phone number")
    private String customerPhone;

    private double totalAmount;
    private LocalDateTime createTime;
    private boolean status = false;
    private String cashier;
    private long stalls ;
    private String stalsName;



   @OneToMany(mappedBy = "billBuyBack", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products = new ArrayList<>(); // List of products in the bill

}
