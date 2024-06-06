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

    private String metalType ;

    @NotNull(message = "Update date cannot be null")
    LocalDateTime updateDate;

    boolean status;
    @OneToMany(mappedBy = "metalPrice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TypeOfMetal> typeOfMetals;

}
