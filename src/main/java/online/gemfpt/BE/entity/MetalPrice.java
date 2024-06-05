package online.gemfpt.BE.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class MetalPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long metalPriceId;

    @NotBlank(message = "Metal type cannot be blank")
    String metalType;

    @Min(value = 0, message = "Sell price must be non-negative")
    double sellPrice;

    @Min(value = 0, message = "Buy price must be non-negative")
    double buyPrice;

    @NotNull (message = "Update date cannot be null")
    LocalDateTime updateDate;

    boolean status;

    @OneToMany(mappedBy = "metalPrice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Metal> metals;
}