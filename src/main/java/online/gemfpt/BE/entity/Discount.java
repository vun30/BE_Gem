package online.gemfpt.BE.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import online.gemfpt.BE.enums.TypeEnum;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank
    private String programName;

    @Min(0)
    @Max(100)
    private double discountRate;

    @NotBlank
    private String description;

    private boolean status;

    @NotBlank
    private String applicableProducts;

    @NotBlank
    private String pointsCondition;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @JsonIgnore
    @OneToMany(mappedBy = "discount", cascade = CascadeType.ALL)
    private List<DiscountProduct> discountProducts;
}
