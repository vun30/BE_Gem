package online.gemfpt.BE.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class TypeOfMetal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long typeId;

    @NotBlank(message = "Metal type cannot be blank")
    String metalType;

    @Min(value = 0, message = "Sell price must be non-negative")
    double sellPrice;

    @Min(value = 0, message = "Buy price must be non-negative")
    double buyPrice;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "metalPriceId")
    private MetalPrice metalPrice;
}
