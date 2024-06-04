package online.gemfpt.BE.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Metal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long metalId;

    @NotBlank
    String name;

    String description;

    @Min(0)
    double weight;

    @NotNull
    double pricePerWeightUnit;

    @NotBlank
    String unit;

    @ManyToOne
    @JoinColumn(name = "productId")
    private Product product;
}
