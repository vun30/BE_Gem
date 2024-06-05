package online.gemfpt.BE.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Gemstone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long gemId;

    @NotBlank
    String description;

    @NotNull
    double price;

    @Min(0)
    int quantity;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "productId")
    private Product product;
}
