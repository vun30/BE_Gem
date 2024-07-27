package online.gemfpt.BE.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private double requestedDiscount;
    private String discountReason;
    private boolean approved;
    private String managerResponse;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime requestTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime responseTime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private LocalDateTime expirationTime;

    private  boolean statusUse;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
}
