package online.gemfpt.BE.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class PromotionProduct {
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
    @JoinColumn(name = "promotion_id", nullable = false)
    @JsonBackReference
    private Promotion promotion;
}
