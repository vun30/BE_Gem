package online.gemfpt.BE.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import online.gemfpt.BE.enums.TypeMoneyChange;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class MoneyChangeHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "stalls_sell_id")
    private StallsSell stallsSell;

     // @Min(value = 0, message = "Amount must be >= 0")
    private double oldTotalInStall;

     //@Min(value = 0, message = "Amount must be >= 0")
    private double amount; // positive for deposit, negative for withdrawal

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime changeDateTime;

    private Long billId;

    private String status;

    private TypeMoneyChange TypeChange;
}
