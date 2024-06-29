package online.gemfpt.BE.entity;

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
    private LocalDateTime requestTime;
    private LocalDateTime responseTime;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
}
