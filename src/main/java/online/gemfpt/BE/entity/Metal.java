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
public class Metal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long metalId;

    @NotBlank
    String name;  ///////* PRODUCT METAL : THIS IS GOLD , SILVER ... NOT METAL MATERIALS *************
    ///////* PRODUCT METAL : THIS IS GOLD , SILVER ... NOT METAL MATERIALS *************
    ///////* PRODUCT METAL : THIS IS GOLD , SILVER ... NOT METAL MATERIALS *************
    ///////* PRODUCT METAL : THIS IS GOLD , SILVER ... NOT METAL MATERIALS *************
    ///////* PRODUCT METAL : THIS IS GOLD , SILVER ... NOT METAL MATERIALS *************
    ///////* PRODUCT METAL : THIS IS GOLD , SILVER ... NOT METAL MATERIALS *************
    ///////* PRODUCT METAL : THIS IS GOLD , SILVER ... NOT METAL MATERIALS *************
    ///////* PRODUCT METAL : THIS IS GOLD , SILVER ... NOT METAL MATERIALS *************
    ///////* PRODUCT METAL : THIS IS GOLD , SILVER ... NOT METAL MATERIALS *************

    String description;

    @Min(0)
    double weight;

    @Transient // Không lưu thuộc tính này vào database
    double pricePerWeightUnit;

    @NotNull
    double unit = 3.75; // 1 chi vàng auto bằng 3,75 gram, unit auto bằng 1 chi vàng

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    @JoinColumn(name = "productId")
    private Product product;

    private double priceMetal;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    @JoinColumn(name = "typeOfMetalId")
    private TypeOfMetal typeOfMetal;

    // Method to calculate price per weight unit
    public double calculatePricePerWeightUnit() {
        return (weight / unit) * typeOfMetal.getSellPrice();
    }

    // Method to set price per weight unit
    public void setPricePerWeightUnit() {
        this.pricePerWeightUnit = calculatePricePerWeightUnit();
    }

    // buy back get set
    public double calculatePriceBuyPerWeightUnit() {
        return (weight / unit) * typeOfMetal.getBuyPrice();
    }

    public void setPriceBuyPerWeightUnit() {
        this.pricePerWeightUnit = calculatePriceBuyPerWeightUnit();
    }
}
