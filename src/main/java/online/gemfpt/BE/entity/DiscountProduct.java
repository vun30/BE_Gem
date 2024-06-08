package online.gemfpt.BE.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class DiscountProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Min(value = 0)
    @Max(value = 100)
    private double discountValue;

    private boolean isActive;

    @ManyToOne
    @JoinColumn(name = "barcode", referencedColumnName = "barcode", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "discount_id", nullable = false)
    private Discount discount;

}
