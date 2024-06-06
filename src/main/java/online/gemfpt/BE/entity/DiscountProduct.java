package online.gemfpt.BE.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Entity
public class DiscountProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Min(value = 0)
    @Max(value = 100)
    private double discountValue;
    private boolean isActive;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "discount_id")
    private Discount discount;

}
