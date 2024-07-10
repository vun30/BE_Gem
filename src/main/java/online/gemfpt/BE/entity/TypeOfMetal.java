package online.gemfpt.BE.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class TypeOfMetal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long typeId;


    String metalType;

    @Min(value = 0, message = "Sell price must be non-negative")
    double sellPrice;

    @Min(value = 0, message = "Buy price must be non-negative")
    double buyPrice;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    @NotNull(message = "Update date cannot be null")
    LocalDateTime updateDate;

    boolean status = getStatus();

    public boolean getStatus() {
        if (metalPrice != null) {
            return metalPrice.isStatus();
        }
        return false; // Default status if MetalPrice is null
    }

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "metalPriceId")
    private MetalPrice metalPrice;
}
