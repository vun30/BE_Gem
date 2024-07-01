package online.gemfpt.BE.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;
import lombok.Data;
import online.gemfpt.BE.entity.Product;
import online.gemfpt.BE.enums.TypeBillEnum;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class BillBuyBackRequest {


    private TypeBillEnum typeBill; // them moi

    private String customerName;
    private int customerPhone;
    private double totalAmount;
    private String voucher;
    private LocalDateTime createTime;
    private boolean status = false;
    private String cashier;
    private long stalls ;
    private String stalsName;


    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products = new ArrayList<>(); // Danh sách các sản phẩm trong hóa đơn mua
}
